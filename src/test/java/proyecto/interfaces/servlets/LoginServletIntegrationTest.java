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
import proyecto.interfaces.utils.PasswordUtil;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

import static org.mockito.Mockito.*;

@Testcontainers
@ExtendWith(MockitoExtension.class)
class LoginServletIntegrationTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("taller_db_test")
      .withUsername("test")
      .withPassword("test");

  private LoginServlet servlet;

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private HttpSession session;
  @Mock
  private RequestDispatcher dispatcher;

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
          "nombre VARCHAR(50), apellido VARCHAR(50), " +
          "correo_electronico VARCHAR(100), password VARCHAR(100), rol VARCHAR(20))");
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    // Inicializamos el Servlet (creará su UsuarioDAO real internamente)
    servlet = new LoginServlet();
    servlet.init();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("TRUNCATE TABLE usuarios");

      // --- INSERCIÓN REAL CON BCRYPT ---
      // Usamos tu utilidad real para generar un Hash válido y guardarlo en la BD de prueba
      String hashReal = PasswordUtil.hashPassword("claveSecreta123");

      String sql = "INSERT INTO usuarios (id_usuario, correo_electronico, password, rol) VALUES (1, 'admin@mail.com', ?, 'ADMIN')";
      try (PreparedStatement pst = conn.prepareStatement(sql)) {
        pst.setString(1, hashReal);
        pst.executeUpdate();
      }
    }
  }

  @Test
  void deberia_IniciarSesionExitosamente_Cuando_LasCredencialesSonReales() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("correoElectronico")).thenReturn("admin@mail.com");

    // Pasamos la contraseña en texto plano, tal como lo haría el usuario en el HTML
    when(request.getParameter("password")).thenReturn("claveSecreta123");

    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    // --- 2. ACT ---
    servlet.doPost(request, response);

    // --- 3. ASSERT ---
    // Verificamos que se guardó el objeto usuario en la sesión
    verify(session).setAttribute(eq("usuarioLogueado"), any());

    // Verificamos que el sistema reconoció el rol y redirigió al menú correcto
    verify(response).sendRedirect("/TallerApp/vistas/admin/menuAdmin.jsp");
  }

  @Test
  void deberia_RechazarElLogin_Cuando_LaContrasenaEsIncorrecta() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("correoElectronico")).thenReturn("admin@mail.com");
    when(request.getParameter("password")).thenReturn("claveEquivocada"); // Clave incorrecta

    when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

    // --- 2. ACT ---
    servlet.doPost(request, response);

    // --- 3. ASSERT ---
    // Comprobamos que el RequestDispatcher intentó devolver al usuario al login
    verify(dispatcher).forward(request, response);

    // Verificamos el mensaje de error exacto
    verify(request).setAttribute(eq("error"), contains("Credenciales incorrectas"));

    // GARANTÍA DE SEGURIDAD: Nunca se debió intentar crear o acceder a la sesión
    verify(request, never()).getSession();
  }
}