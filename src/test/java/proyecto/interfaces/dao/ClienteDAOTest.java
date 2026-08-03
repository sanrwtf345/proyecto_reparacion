package proyecto.interfaces.dao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import proyecto.interfaces.AdminConexion;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Usuario;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class ClienteDAOTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("taller_db_test")
      .withUsername("test")
      .withPassword("test");

  private ClienteDAO clienteDAO;

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

      // 1. Creamos la tabla usuarios PRIMERO (por la dependencia de la Clave Foránea)
      st.execute("CREATE TABLE usuarios (" +
          "id_usuario INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(50), apellido VARCHAR(50), " +
          "correo_electronico VARCHAR(100), password VARCHAR(100), rol VARCHAR(20))");

      // 2. Creamos la tabla clientes haciendo referencia al id_usuario
      st.execute("CREATE TABLE clientes (" +
          "id_cliente INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(50), " +
          "apellido VARCHAR(50), " +
          "telefono VARCHAR(20), " +
          "email VARCHAR(50), " +
          "id_usuario INT, " +
          "FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario))");
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    clienteDAO = new ClienteDAO();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      // Desactivamos la validación de FKs temporalmente para truncar tablas
      st.execute("SET FOREIGN_KEY_CHECKS = 0");
      st.execute("TRUNCATE TABLE clientes");
      st.execute("TRUNCATE TABLE usuarios");
      st.execute("SET FOREIGN_KEY_CHECKS = 1");

      // Insertamos un Usuario semilla (ID=1) para poder asignárselo a los clientes en los tests
      st.execute("INSERT INTO usuarios (id_usuario, nombre) VALUES (1, 'Recepcionista')");
    }
  }

  @Test
  void deberia_InsertarYObtenerCliente_Cuando_DatosSonValidos() {
    // --- 1. ARRANGE ---
    Cliente nuevoCliente = new Cliente();
    nuevoCliente.setNombre("Lionel");
    nuevoCliente.setApellido("Messi");
    nuevoCliente.setTelefono("123456789");
    nuevoCliente.setEmail("leo@mail.com");

    // Le asignamos el usuario semilla que existe en la BD
    Usuario usuarioVinculado = new Usuario();
    usuarioVinculado.setIdUsuario(1);
    nuevoCliente.setUsuario(usuarioVinculado);

    // --- 2. ACT ---
    clienteDAO.insert(nuevoCliente);
    Integer idGenerado = nuevoCliente.getIdCliente();

    Cliente clienteRecuperado = clienteDAO.getById(idGenerado);

    // --- 3. ASSERT ---
    assertThat(idGenerado).isGreaterThan(0);
    assertThat(clienteDAO.existsById(idGenerado)).isTrue();

    assertThat(clienteRecuperado).isNotNull();
    assertThat(clienteRecuperado.getNombre()).isEqualTo("Lionel");
    assertThat(clienteRecuperado.getApellido()).isEqualTo("Messi");
    assertThat(clienteRecuperado.getUsuario().getIdUsuario()).isEqualTo(1);
  }

  @Test
  void deberia_ActualizarCliente_Cuando_SeModificanSusDatos() {
    // --- 1. ARRANGE ---
    Cliente cliente = crearClienteBasico("Angel", "Di Maria");
    clienteDAO.insert(cliente);

    Cliente aModificar = clienteDAO.getById(cliente.getIdCliente());
    aModificar.setTelefono("9999999");
    aModificar.setEmail("fideo@mail.com");

    // --- 2. ACT ---
    clienteDAO.update(aModificar);

    // --- 3. ASSERT ---
    Cliente modificado = clienteDAO.getById(cliente.getIdCliente());
    assertThat(modificado.getTelefono()).isEqualTo("9999999");
    assertThat(modificado.getEmail()).isEqualTo("fideo@mail.com");
  }

  @Test
  void deberia_EliminarCliente_Cuando_SeProporcionaUnIdValido() {
    // --- 1. ARRANGE ---
    Cliente cliente = crearClienteBasico("Dibu", "Martinez");
    clienteDAO.insert(cliente);
    Integer id = cliente.getIdCliente();

    assertThat(clienteDAO.existsById(id)).isTrue();

    // --- 2. ACT ---
    clienteDAO.delete(id);

    // --- 3. ASSERT ---
    assertThat(clienteDAO.existsById(id)).isFalse();
    assertThat(clienteDAO.getById(id)).isNull();
  }

  @Test
  void deberia_BuscarClientesPorApellido_UsandoLike() {
    // --- 1. ARRANGE ---
    clienteDAO.insert(crearClienteBasico("Juan", "Gomez"));
    clienteDAO.insert(crearClienteBasico("Pedro", "Gonzalez"));
    clienteDAO.insert(crearClienteBasico("Luis", "Perez"));

    // --- 2. ACT ---
    // Buscamos "Gon" para ver si trae a Gonzalez pero no a Gomez
    List<Cliente> resultados = clienteDAO.getByApellido("Gon");

    // --- 3. ASSERT ---
    assertThat(resultados).hasSize(1);
    assertThat(resultados.get(0).getApellido()).isEqualTo("Gonzalez");
  }

  @Test
  void deberia_ObtenerTodosLosClientes() {
    // --- 1. ARRANGE ---
    clienteDAO.insert(crearClienteBasico("Juan", "A"));
    clienteDAO.insert(crearClienteBasico("Pedro", "B"));

    // --- 2. ACT ---
    List<Cliente> todos = clienteDAO.getAll();

    // --- 3. ASSERT ---
    assertThat(todos).hasSize(2);
  }

  // --- MÉTODO AUXILIAR ---
  private Cliente crearClienteBasico(String nombre, String apellido) {
    Cliente c = new Cliente();
    c.setNombre(nombre);
    c.setApellido(apellido);
    c.setTelefono("000");
    c.setEmail("test@test.com");

    Usuario u = new Usuario();
    u.setIdUsuario(1);
    c.setUsuario(u);
    return c;
  }
}