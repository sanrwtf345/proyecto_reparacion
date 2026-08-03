package proyecto.interfaces.servlets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import proyecto.interfaces.AdminConexion;
import proyecto.interfaces.dao.UsuarioDAO;
import proyecto.interfaces.entities.Usuario;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Testcontainers
@ExtendWith(MockitoExtension.class)
class UsuarioServletIntegrationTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("taller_db_test")
      .withUsername("test")
      .withPassword("test");

  private UsuarioServlet servlet;
  private UsuarioDAO usuarioDAO;

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private HttpSession session;
  @Mock
  private RequestDispatcher dispatcher; // <--- Agregamos el Dispatcher para probar las vistas

  @BeforeAll
  static void setupDatabase() throws Exception {
    System.setProperty("db.url", mysql.getJdbcUrl());
    System.setProperty("db.user", mysql.getUsername());
    System.setProperty("db.password", mysql.getPassword());

    AdminConexion.INSTANCE.recargarPoolParaTests();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("CREATE TABLE usuarios (" +
          "id_usuario INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(50), apellido VARCHAR(50), " +
          "correo_electronico VARCHAR(100), password VARCHAR(100), rol VARCHAR(20))");
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    servlet = new UsuarioServlet();
    servlet.init();

    usuarioDAO = new UsuarioDAO();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("TRUNCATE TABLE usuarios");

      st.execute("INSERT INTO usuarios (id_usuario, correo_electronico, nombre, apellido, rol) VALUES (1, 'admin@mail.com', 'Admin', 'Jefe', 'ADMIN')");
      st.execute("INSERT INTO usuarios (id_usuario, correo_electronico, nombre, apellido, rol) VALUES (2, 'borrar@mail.com', 'Test', 'Borrar', 'TECNICO')");
    }
  }

  // --- PRUEBAS ORIGINALES (Mantenidas) ---

  @Test
  void deberia_GuardarNuevoUsuario_ConPasswordHasheado_EnLaBDReal() throws Exception {
    when(request.getParameter("action")).thenReturn("guardar");
    when(request.getParameter("correoElectronico")).thenReturn("nuevo@mail.com");
    when(request.getParameter("password")).thenReturn("claveSegura123");
    when(request.getParameter("nombre")).thenReturn("Ana");
    when(request.getParameter("apellido")).thenReturn("Gomez");
    when(request.getParameter("rol")).thenReturn("TECNICO");

    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    servlet.doPost(request, response);

    verify(response).sendRedirect("/TallerApp/UsuariosController?action=listar");

    Usuario usuarioGuardado = usuarioDAO.getByCorreoElectronico("nuevo@mail.com");
    assertThat(usuarioGuardado).isNotNull();
    assertThat(usuarioGuardado.getNombre()).isEqualTo("Ana");
    assertThat(usuarioGuardado.getPassword()).isNotEqualTo("claveSegura123");
  }

  @Test
  void deberia_EliminarUsuarioEnBDReal_CuandoNoEsElMismoUsuarioLogueado() throws Exception {
    when(request.getParameter("action")).thenReturn("eliminar");
    when(request.getParameter("idUsuario")).thenReturn("2");

    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    Usuario adminLogueado = new Usuario();
    adminLogueado.setIdUsuario(1);
    when(session.getAttribute("usuarioLogueado")).thenReturn(adminLogueado);

    servlet.doGet(request, response);

    verify(response).sendRedirect("/TallerApp/UsuariosController?action=listar");
    assertThat(usuarioDAO.existsById(2)).isFalse();
  }

  @Test
  void deberia_EvitarEliminarAlUsuario_CuandoIntentaBorrarseASiMismo() throws Exception {
    when(request.getParameter("action")).thenReturn("eliminar");
    when(request.getParameter("idUsuario")).thenReturn("1");

    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    Usuario adminLogueado = new Usuario();
    adminLogueado.setIdUsuario(1);
    when(session.getAttribute("usuarioLogueado")).thenReturn(adminLogueado);

    servlet.doGet(request, response);

    verify(response).sendRedirect("/TallerApp/UsuariosController?action=listar");
    verify(session).setAttribute(eq("error"), contains("No puedes eliminar tu propia cuenta"));
    assertThat(usuarioDAO.existsById(1)).isTrue();
  }

  // --- NUEVAS PRUEBAS PARA SUBIR LA COBERTURA AL 80%+ ---

  @Test
  void deberia_ListarUsuariosYEnviarAlJsp() throws Exception {
    when(request.getParameter("action")).thenReturn("listar");
    when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

    servlet.doGet(request, response);

    // Verificamos que se guarden los usuarios en el request y se llame al dispatcher
    verify(request).setAttribute(eq("listaUsuarios"), anyList());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_MostrarFormularioParaRegistrarNuevoUsuario() throws Exception {
    when(request.getParameter("action")).thenReturn("formularioRegisUsuario");
    when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

    servlet.doGet(request, response);

    // Verifica que el título sea el de "Nuevo Usuario"
    verify(request).setAttribute(eq("titulo"), contains("Registrar Nuevo Usuario"));
    verify(request).setAttribute(eq("usuario"), any(Usuario.class));
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_MostrarFormularioParaEditarUsuarioExistente() throws Exception {
    when(request.getParameter("action")).thenReturn("editar");
    when(request.getParameter("idUsuario")).thenReturn("1");
    when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

    servlet.doGet(request, response);

    // Verifica que el título traiga el correo del usuario ID 1
    verify(request).setAttribute(eq("titulo"), contains("admin@mail.com"));
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_RechazarGuardado_Cuando_FaltanDatosObligatorios() throws Exception {
    when(request.getParameter("action")).thenReturn("guardar");
    when(request.getParameter("correoElectronico")).thenReturn(""); // CAMPO VACÍO

    when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

    servlet.doPost(request, response);

    // Verifica que atrapó la validación y guardó el mensaje de error
    verify(request).setAttribute(eq("error"), contains("Todos los campos son obligatorios"));
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_ActualizarUsuario_EnLaBaseDeDatosReal() throws Exception {
    when(request.getParameter("action")).thenReturn("actualizar");
    when(request.getParameter("idUsuario")).thenReturn("2"); // Actualizamos al usuario de Test
    when(request.getParameter("correoElectronico")).thenReturn("editado@mail.com");
    when(request.getParameter("nombre")).thenReturn("Roberto");
    when(request.getParameter("apellido")).thenReturn("Carlos");
    when(request.getParameter("rol")).thenReturn("TECNICO");
    when(request.getParameter("password")).thenReturn(""); // Sin cambio de contraseña

    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    servlet.doPost(request, response);

    verify(response).sendRedirect("/TallerApp/UsuariosController?action=listar");

    // Vamos a la BD a confirmar que el usuario 2 cambió su nombre
    Usuario actualizado = usuarioDAO.getById(2);
    assertThat(actualizado.getNombre()).isEqualTo("Roberto");
    assertThat(actualizado.getCorreoElectronico()).isEqualTo("editado@mail.com");
  }

  @Test
  void deberia_ManejarErrorInterno_Cuando_IdUsuarioEsInvalido() throws Exception {
    // Arrange
    when(request.getParameter("action")).thenReturn("editar");
    when(request.getParameter("idUsuario")).thenReturn("abc"); // Letras en vez de números provocan un error

    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    // Act
    servlet.doGet(request, response);

    // Assert: Verificamos que entró al catch y preparó el mensaje de error
    verify(session).setAttribute(eq("error"), contains("Error interno"));
    verify(response).sendRedirect("/TallerApp/UsuariosController?action=listar");
  }

}