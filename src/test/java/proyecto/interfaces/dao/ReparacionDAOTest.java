package proyecto.interfaces.dao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import proyecto.interfaces.AdminConexion;
import proyecto.interfaces.entities.Equipo;
import proyecto.interfaces.entities.Reparacion;
import proyecto.interfaces.entities.Usuario;
import proyecto.interfaces.enums.EstadoReparacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class ReparacionDAOTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("taller_db_test")
      .withUsername("test")
      .withPassword("test");

  private ReparacionDAO reparacionDAO;

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

      // 1. Creamos TODA la estructura de tablas relacionales necesarias
      st.execute("CREATE TABLE clientes (" +
          "id_cliente INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(50), apellido VARCHAR(50), " +
          "telefono VARCHAR(20), email VARCHAR(50))");

      st.execute("CREATE TABLE usuarios (" +
          "id_usuario INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(50), apellido VARCHAR(50), " +
          "correo_electronico VARCHAR(100), password VARCHAR(100), rol VARCHAR(20))");

      st.execute("CREATE TABLE equipo (" +
          "id_equipo INT AUTO_INCREMENT PRIMARY KEY, " +
          "id_cliente INT, tipo_equipo VARCHAR(50), " +
          "marca VARCHAR(50), modelo VARCHAR(50), " +
          "num_serie VARCHAR(50), problema_reportado VARCHAR(255), " +
          "FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente))");

      st.execute("CREATE TABLE reparacion (" +
          "id_reparacion INT AUTO_INCREMENT PRIMARY KEY, " +
          "id_equipo INT, id_usuario INT, " +
          "diagnostico_final VARCHAR(255), estado VARCHAR(50), " +
          "costo_repuestos DECIMAL(10,2), costo_mano_obra DECIMAL(10,2), " +
          "presupuesto_total DECIMAL(10,2), " +
          "fecha_entrega_estimada DATE, fecha_diagnostico DATE, " +
          "fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
          "FOREIGN KEY (id_equipo) REFERENCES equipo(id_equipo), " +
          "FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario))");
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    reparacionDAO = new ReparacionDAO();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      // 2. Apagamos validación de FKs temporalmente para vaciar las tablas
      st.execute("SET FOREIGN_KEY_CHECKS = 0");
      st.execute("TRUNCATE TABLE reparacion");
      st.execute("TRUNCATE TABLE equipo");
      st.execute("TRUNCATE TABLE usuarios");
      st.execute("TRUNCATE TABLE clientes");
      st.execute("SET FOREIGN_KEY_CHECKS = 1");

      // 3. Cargamos "datos semilla" para que existan el Cliente 1, Usuario 1 y Equipo 1
      st.execute("INSERT INTO clientes (id_cliente, nombre, apellido) VALUES (1, 'Lionel', 'Messi')");
      st.execute("INSERT INTO usuarios (id_usuario, nombre) VALUES (1, 'Tecnico1')");
      st.execute("INSERT INTO equipo (id_equipo, id_cliente, tipo_equipo) VALUES (1, 1, 'Notebook')");
    }
  }

  @Test
  void deberia_InsertarYObtenerReparacionPorId_ResolviendoLosJoins() {
    // --- 1. ARRANGE ---
    Reparacion nuevaReparacion = new Reparacion();

    Equipo equipoRelacionado = new Equipo();
    equipoRelacionado.setIdEquipo(1); // El que creamos en el BeforeEach
    nuevaReparacion.setEquipo(equipoRelacionado);

    Usuario usuarioRelacionado = new Usuario();
    usuarioRelacionado.setIdUsuario(1); // El que creamos en el BeforeEach
    nuevaReparacion.setUsuario(usuarioRelacionado);

    nuevaReparacion.setDiagnosticoFinal("Falla de RAM");
    nuevaReparacion.setEstado(EstadoReparacion.PENDIENTE);
    nuevaReparacion.setCostoRepuestos(new BigDecimal("15000.00"));
    nuevaReparacion.setCostoManoObra(new BigDecimal("5000.00"));
    nuevaReparacion.setPresupuestoTotal(new BigDecimal("20000.00"));
    nuevaReparacion.setFechaDiagnostico(LocalDate.now());

    // --- 2. ACT ---
    reparacionDAO.insert(nuevaReparacion);

    Integer idGenerado = nuevaReparacion.getIdReparacion();
    Reparacion reparacionRecuperada = reparacionDAO.getById(idGenerado);

    // --- 3. ASSERT ---
    assertThat(idGenerado).isGreaterThan(0);
    assertThat(reparacionRecuperada).isNotNull();
    assertThat(reparacionRecuperada.getDiagnosticoFinal()).isEqualTo("Falla de RAM");

    // Verificamos que el mapeo JOIN recuperó los datos asociados correctamente
    assertThat(reparacionRecuperada.getEquipo().getTipoEquipo()).isEqualTo("Notebook");
    assertThat(reparacionRecuperada.getUsuario().getNombre()).isEqualTo("Tecnico1");
    assertThat(reparacionRecuperada.getEquipo().getCliente().getNombre()).isEqualTo("Lionel");
  }

  @Test
  void deberia_FiltrarReparacionesPorEstado() {
    // --- 1. ARRANGE ---
    Reparacion rep1 = crearReparacionBasicaConEstado(EstadoReparacion.TERMINADO);
    Reparacion rep2 = crearReparacionBasicaConEstado(EstadoReparacion.PENDIENTE);
    Reparacion rep3 = crearReparacionBasicaConEstado(EstadoReparacion.TERMINADO);

    reparacionDAO.insert(rep1);
    reparacionDAO.insert(rep2);
    reparacionDAO.insert(rep3);

    // --- 2. ACT ---
    List<Reparacion> terminadas = reparacionDAO.getByEstado(EstadoReparacion.TERMINADO);

    // --- 3. ASSERT ---
    assertThat(terminadas).hasSize(2);
    assertThat(terminadas).allMatch(r -> r.getEstado() == EstadoReparacion.TERMINADO);
  }

  @Test
  void deberia_ActualizarReparacionCorrectamente() {
    // --- 1. ARRANGE ---
    Reparacion rep = crearReparacionBasicaConEstado(EstadoReparacion.PENDIENTE);
    reparacionDAO.insert(rep);

    Reparacion aModificar = reparacionDAO.getById(rep.getIdReparacion());
    aModificar.setEstado(EstadoReparacion.EN_PROCESO);
    aModificar.setDiagnosticoFinal("Cambio de disco duro");

    // --- 2. ACT ---
    reparacionDAO.update(aModificar);

    // --- 3. ASSERT ---
    Reparacion modificada = reparacionDAO.getById(rep.getIdReparacion());
    assertThat(modificada.getEstado()).isEqualTo(EstadoReparacion.EN_PROCESO);
    assertThat(modificada.getDiagnosticoFinal()).isEqualTo("Cambio de disco duro");
  }

  // Método auxiliar (Factory) para no repetir código creando objetos básicos en los tests
  private Reparacion crearReparacionBasicaConEstado(EstadoReparacion estado) {
    Reparacion r = new Reparacion();
    Equipo e = new Equipo(); e.setIdEquipo(1);
    Usuario u = new Usuario(); u.setIdUsuario(1);

    r.setEquipo(e);
    r.setUsuario(u);
    r.setEstado(estado);
    return r;
  }
}