package proyecto.interfaces.dao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import proyecto.interfaces.AdminConexion;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Equipo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class EquipoDAOTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("taller_db_test")
      .withUsername("test")
      .withPassword("test");

  private EquipoDAO equipoDAO;

  @BeforeAll
  static void setupDatabase() throws Exception {
    System.setProperty("db.url", mysql.getJdbcUrl());
    System.setProperty("db.user", mysql.getUsername());
    System.setProperty("db.password", mysql.getPassword());

    // ---> AGREGA ESTA LÍNEA EXACTAMENTE AQUÍ <---
    AdminConexion.INSTANCE.recargarPoolParaTests();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {
      // ... (resto de las creaciones de tablas)

      // 1. Usuarios (Nivel más bajo de dependencia)
      st.execute("CREATE TABLE usuarios (" +
          "id_usuario INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(50), apellido VARCHAR(50), " +
          "correo_electronico VARCHAR(100), password VARCHAR(100), rol VARCHAR(20))");

      // 2. Clientes (Depende de Usuarios)
      st.execute("CREATE TABLE clientes (" +
          "id_cliente INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(50), apellido VARCHAR(50), " +
          "telefono VARCHAR(20), email VARCHAR(50), id_usuario INT, " +
          "FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario))");

      // 3. Equipo (Depende de Clientes). Incluimos fecha_registro para el ORDER BY del getAll()
      st.execute("CREATE TABLE equipo (" +
          "id_equipo INT AUTO_INCREMENT PRIMARY KEY, " +
          "id_cliente INT, " +
          "tipo_equipo VARCHAR(50), " +
          "marca VARCHAR(50), " +
          "modelo VARCHAR(50), " +
          "num_serie VARCHAR(50), " +
          "problema_reportado VARCHAR(255), " +
          "fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
          "FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente))");
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    equipoDAO = new EquipoDAO();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      // Desactivamos FKs para limpiar las tablas
      st.execute("SET FOREIGN_KEY_CHECKS = 0");
      st.execute("TRUNCATE TABLE equipo");
      st.execute("TRUNCATE TABLE clientes");
      st.execute("TRUNCATE TABLE usuarios");
      st.execute("SET FOREIGN_KEY_CHECKS = 1");

      // Insertamos datos semilla
      st.execute("INSERT INTO usuarios (id_usuario, nombre) VALUES (1, 'TecnicoAdmin')");
      st.execute("INSERT INTO clientes (id_cliente, nombre, apellido, id_usuario) VALUES (1, 'Juan', 'Perez', 1)");
      // Un segundo cliente para probar los filtros
      st.execute("INSERT INTO clientes (id_cliente, nombre, apellido, id_usuario) VALUES (2, 'Maria', 'Gomez', 1)");
    }
  }

  @Test
  void deberia_InsertarYObtenerEquipoPorId_Cuando_DatosSonValidos() {
    // --- 1. ARRANGE ---
    Equipo nuevoEquipo = new Equipo();
    nuevoEquipo.setTipoEquipo("Notebook");
    nuevoEquipo.setMarca("Dell");
    nuevoEquipo.setModelo("Inspiron");
    nuevoEquipo.setNumeroSerie("SN-12345");
    nuevoEquipo.setProblemaReportado("No enciende");

    // Asociamos al cliente semilla ID=1
    Cliente clienteAsociado = new Cliente();
    clienteAsociado.setIdCliente(1);
    nuevoEquipo.setCliente(clienteAsociado);

    // --- 2. ACT ---
    equipoDAO.insert(nuevoEquipo);
    Integer idGenerado = nuevoEquipo.getIdEquipo();

    Equipo equipoRecuperado = equipoDAO.getById(idGenerado);

    // --- 3. ASSERT ---
    assertThat(idGenerado).isGreaterThan(0);
    assertThat(equipoRecuperado).isNotNull();
    assertThat(equipoRecuperado.getMarca()).isEqualTo("Dell");
    assertThat(equipoRecuperado.getProblemaReportado()).isEqualTo("No enciende");
    assertThat(equipoRecuperado.getCliente().getIdCliente()).isEqualTo(1);
  }

  @Test
  void deberia_ActualizarEquipo_Cuando_SeModificanSusDatos() {
    // --- 1. ARRANGE ---
    Equipo equipo = crearEquipoBasico(1, "PC Escritorio", "Falla disco");
    equipoDAO.insert(equipo);

    Equipo aModificar = equipoDAO.getById(equipo.getIdEquipo());
    aModificar.setProblemaReportado("Fuente quemada");
    aModificar.setMarca("HP");

    // --- 2. ACT ---
    equipoDAO.update(aModificar);

    // --- 3. ASSERT ---
    Equipo modificado = equipoDAO.getById(equipo.getIdEquipo());
    assertThat(modificado.getProblemaReportado()).isEqualTo("Fuente quemada");
    assertThat(modificado.getMarca()).isEqualTo("HP");
  }

  @Test
  void deberia_EliminarEquipo_Cuando_SeProporcionaIdValido() {
    // --- 1. ARRANGE ---
    Equipo equipo = crearEquipoBasico(1, "Monitor", "No da video");
    equipoDAO.insert(equipo);
    Integer id = equipo.getIdEquipo();

    assertThat(equipoDAO.existsById(id)).isTrue();

    // --- 2. ACT ---
    equipoDAO.delete(id);

    // --- 3. ASSERT ---
    assertThat(equipoDAO.existsById(id)).isFalse();
    assertThat(equipoDAO.getById(id)).isNull();
  }

  @Test
  void deberia_ListarEquiposPorClienteId() {
    // --- 1. ARRANGE ---
    // 2 equipos para el cliente 1
    equipoDAO.insert(crearEquipoBasico(1, "Notebook", "Pantalla rota"));
    equipoDAO.insert(crearEquipoBasico(1, "Tablet", "Pin de carga"));
    // 1 equipo para el cliente 2
    equipoDAO.insert(crearEquipoBasico(2, "Impresora", "Atasco de papel"));

    // --- 2. ACT ---
    List<Equipo> equiposCliente1 = equipoDAO.getByClienteId(1);
    List<Equipo> equiposCliente2 = equipoDAO.getByClienteId(2);

    // --- 3. ASSERT ---
    assertThat(equiposCliente1).hasSize(2);
    assertThat(equiposCliente2).hasSize(1);
    assertThat(equiposCliente2.get(0).getTipoEquipo()).isEqualTo("Impresora");
  }

  @Test
  void deberia_ObtenerTodosLosEquipos_ResolviendoElJoinConCliente() {
    // --- 1. ARRANGE ---
    equipoDAO.insert(crearEquipoBasico(1, "Consola", "Luz roja"));

    // --- 2. ACT ---
    List<Equipo> todos = equipoDAO.getAll();

    // --- 3. ASSERT ---
    assertThat(todos).hasSize(1);

    // Verificamos que el JOIN haya traído correctamente los datos del cliente
    Cliente clienteAsociado = todos.get(0).getCliente();
    assertThat(clienteAsociado.getNombre()).isEqualTo("Juan");
    assertThat(clienteAsociado.getApellido()).isEqualTo("Perez");
  }

  // --- MÉTODO AUXILIAR ---
  private Equipo crearEquipoBasico(int idCliente, String tipo, String problema) {
    Equipo e = new Equipo();
    e.setTipoEquipo(tipo);
    e.setMarca("Genérica");
    e.setModelo("Genérico");
    e.setNumeroSerie("0000");
    e.setProblemaReportado(problema);

    Cliente c = new Cliente();
    c.setIdCliente(idCliente);
    e.setCliente(c);

    return e;
  }
}