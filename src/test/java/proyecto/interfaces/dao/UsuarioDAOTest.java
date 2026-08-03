package proyecto.interfaces.dao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import proyecto.interfaces.AdminConexion;
import proyecto.interfaces.entities.Usuario;
import proyecto.interfaces.enums.RolUsuario;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 1. Activamos Testcontainers para esta clase
@Testcontainers
class UsuarioDAOTest {

  // 2. Definimos el contenedor Docker con MySQL real
  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("taller_db_test")
      .withUsername("test")
      .withPassword("test");

  private UsuarioDAO usuarioDAO;

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
      st.execute("CREATE TABLE usuarios (" +
          "id_usuario INT AUTO_INCREMENT PRIMARY KEY, " +
          "correo_electronico VARCHAR(100) UNIQUE, " +
          "password VARCHAR(100), " +
          "nombre VARCHAR(50), " +
          "apellido VARCHAR(50), " +
          "rol VARCHAR(20))");
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    usuarioDAO = new UsuarioDAO();
    // 5. Principio F.I.R.S.T (Independent): Limpiamos la tabla antes de cada test
    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {
      st.execute("TRUNCATE TABLE usuarios");
    }
  }

  @Test
  void deberia_InsertarYBuscarUsuarioPorCorreo_Cuando_DatosSonValidos() {
    // --- 1. ARRANGE ---
    Usuario nuevoUsuario = new Usuario("tec@mail.com", "hash123", "Carlos", "Sainz", RolUsuario.TECNICO);

    // --- 2. ACT ---
    usuarioDAO.insert(nuevoUsuario);
    Usuario usuarioRecuperado = usuarioDAO.getByCorreoElectronico("tec@mail.com");

    // --- 3. ASSERT ---
    // Comprobamos que el ID se generó automáticamente y se asignó al objeto
    assertThat(nuevoUsuario.getIdUsuario()).isGreaterThan(0);

    // Verificamos que los datos persistidos en MySQL son exactamente los mismos
    assertThat(usuarioRecuperado).isNotNull();
    assertThat(usuarioRecuperado.getNombre()).isEqualTo("Carlos");
    assertThat(usuarioRecuperado.getApellido()).isEqualTo("Sainz");
    assertThat(usuarioRecuperado.getRol()).isEqualTo(RolUsuario.TECNICO);
  }

  @Test
  void deberia_ActualizarDatosDelUsuario_Cuando_ElUsuarioExiste() {
    // --- 1. ARRANGE ---
    Usuario usuarioOriginal = new Usuario("admin@mail.com", "clave", "Jefe", "Gomez", RolUsuario.ADMIN);
    usuarioDAO.insert(usuarioOriginal);

    // Modificamos el objeto recuperado
    Usuario usuarioAEditar = usuarioDAO.getByCorreoElectronico("admin@mail.com");
    usuarioAEditar.setNombre("SuperJefe");
    usuarioAEditar.setRol(RolUsuario.TECNICO);

    // --- 2. ACT ---
    usuarioDAO.update(usuarioAEditar);

    // --- 3. ASSERT ---
    Usuario usuarioModificado = usuarioDAO.getById(usuarioAEditar.getIdUsuario());
    assertThat(usuarioModificado.getNombre()).isEqualTo("SuperJefe");
    assertThat(usuarioModificado.getRol()).isEqualTo(RolUsuario.TECNICO);
  }

  @Test
  void deberia_EliminarUsuario_Cuando_SeProporcionaIdValido() {
    // --- 1. ARRANGE ---
    Usuario usuario = new Usuario("borrar@mail.com", "123", "Juan", "Perez", RolUsuario.TECNICO);
    usuarioDAO.insert(usuario);
    Integer idGenerado = usuario.getIdUsuario();

    // Confirmamos que existe
    assertThat(usuarioDAO.existsById(idGenerado)).isTrue();

    // --- 2. ACT ---
    usuarioDAO.delete(idGenerado);

    // --- 3. ASSERT ---
    // Comprobamos que ya no existe en la base de datos real
    assertThat(usuarioDAO.existsById(idGenerado)).isFalse();
    assertThat(usuarioDAO.getById(idGenerado)).isNull();
  }

  @Test
  void deberia_BuscarUsuariosPorApellido_UsandoLike() {
    // --- 1. ARRANGE ---
    usuarioDAO.insert(new Usuario("1@mail.com", "123", "Ana", "Martinez", RolUsuario.TECNICO));
    usuarioDAO.insert(new Usuario("2@mail.com", "123", "Luis", "Martinez", RolUsuario.TECNICO));
    usuarioDAO.insert(new Usuario("3@mail.com", "123", "Pedro", "Gomez", RolUsuario.TECNICO));

    // --- 2. ACT ---
    // Buscamos "Mart" para validar que el %LIKE% funciona
    List<Usuario> resultados = usuarioDAO.getByApellido("Mart");

    // --- 3. ASSERT ---
    assertThat(resultados).hasSize(2); // Debería encontrar a Ana y Luis
    // Extraemos los nombres de la lista de resultados para verificarlos rápidamente
    assertThat(resultados)
        .extracting(Usuario::getNombre)
        .containsExactlyInAnyOrder("Ana", "Luis");
  }
}