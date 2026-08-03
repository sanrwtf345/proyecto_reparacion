package proyecto.interfaces.servlets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import proyecto.interfaces.dao.UsuarioDAO;
import proyecto.interfaces.entities.Usuario;
import proyecto.interfaces.enums.RolUsuario;
import proyecto.interfaces.utils.PasswordUtil;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServletTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpSession session;

  @Mock
  private RequestDispatcher dispatcher;

  @Mock
  private UsuarioDAO usuarioDAO;

  @InjectMocks
  private LoginServlet servlet;

  @Test
  void deberia_RedirigirAMenuAdmin_Cuando_CredencialesSonCorrectasYEsAdmin() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("correoElectronico")).thenReturn("admin@mail.com");
    when(request.getParameter("password")).thenReturn("clave123");

    Usuario adminMock = new Usuario();
    adminMock.setRol(RolUsuario.ADMIN);
    adminMock.setPassword("hash_de_bd");

    when(usuarioDAO.getByCorreoElectronico("admin@mail.com")).thenReturn(adminMock);
    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    // FINGIMOS EL MÉTODO ESTÁTICO: Le decimos que la validación de contraseña siempre dé 'true' en este test
    try (MockedStatic<PasswordUtil> mockedPassword = mockStatic(PasswordUtil.class)) {
      mockedPassword.when(() -> PasswordUtil.verifyPassword("clave123", "hash_de_bd")).thenReturn(true);

      // --- 2. ACT ---
      servlet.doPost(request, response);

      // --- 3. ASSERT ---
      verify(session).setAttribute("usuarioLogueado", adminMock);
      verify(response).sendRedirect("/TallerApp/vistas/admin/menuAdmin.jsp");
    }
  }

  @Test
  void deberia_RedirigirAMenuTecnico_Cuando_CredencialesSonCorrectasYEsTecnico() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("correoElectronico")).thenReturn("tecnico@mail.com");
    when(request.getParameter("password")).thenReturn("clave123");

    Usuario tecnicoMock = new Usuario();
    tecnicoMock.setRol(RolUsuario.TECNICO);
    tecnicoMock.setPassword("hash_de_bd");

    when(usuarioDAO.getByCorreoElectronico("tecnico@mail.com")).thenReturn(tecnicoMock);
    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    try (MockedStatic<PasswordUtil> mockedPassword = mockStatic(PasswordUtil.class)) {
      mockedPassword.when(() -> PasswordUtil.verifyPassword("clave123", "hash_de_bd")).thenReturn(true);

      // --- 2. ACT ---
      servlet.doPost(request, response);

      // --- 3. ASSERT ---
      verify(session).setAttribute("usuarioLogueado", tecnicoMock);
      verify(response).sendRedirect("/TallerApp/vistas/tecnico/menuTecnico.jsp");
    }
  }

  @Test
  void deberia_MostrarError_Cuando_LaContrasenaEsIncorrecta() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("correoElectronico")).thenReturn("admin@mail.com");
    when(request.getParameter("password")).thenReturn("claveEquivocada");

    Usuario usuarioMock = new Usuario();
    usuarioMock.setPassword("hash_de_bd");

    when(usuarioDAO.getByCorreoElectronico("admin@mail.com")).thenReturn(usuarioMock);
    when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

    // Simulamos que la verificación de BCrypt falla (retorna false)
    try (MockedStatic<PasswordUtil> mockedPassword = mockStatic(PasswordUtil.class)) {
      mockedPassword.when(() -> PasswordUtil.verifyPassword("claveEquivocada", "hash_de_bd")).thenReturn(false);

      // --- 2. ACT ---
      servlet.doPost(request, response);

      // --- 3. ASSERT ---
      verify(request).setAttribute(eq("error"), contains("Credenciales incorrectas"));
      verify(dispatcher).forward(request, response);
      // Comprobamos que NUNCA se creó la sesión
      verify(request, never()).getSession();
    }
  }

  @Test
  void deberia_MostrarError_Cuando_ElDAOArrojaUnaExcepcion() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("correoElectronico")).thenReturn("error@mail.com");

    // Forzamos al DAO a que falle simulando una caída de la base de datos
    when(usuarioDAO.getByCorreoElectronico("error@mail.com")).thenThrow(new RuntimeException("Base de datos desconectada"));

    when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

    // --- 2. ACT ---
    servlet.doPost(request, response);

    // --- 3. ASSERT ---
    // Verificamos que el catch() atrape el error y lo mande a la vista
    verify(request).setAttribute(eq("error"), contains("Ocurrió un error en el servidor"));
    verify(dispatcher).forward(request, response);
  }
}