package proyecto.interfaces.servlets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServletTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpSession session;

  @InjectMocks
  private LogoutServlet servlet;

  @Test
  void deberia_InvalidarSesionYRedirigir_Cuando_HayUnaSesionActiva() throws Exception {
    // --- 1. ARRANGE ---
    // request.getSession(false) devuelve la sesión actual sin crear una nueva.
    // Simulamos que el usuario SÍ tiene una sesión activa.
    when(request.getSession(false)).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    // --- 2. ACT ---
    servlet.doGet(request, response);

    // --- 3. ASSERT ---
    // Verificamos que se haya destruido la sesión para borrar los datos
    verify(session).invalidate();

    // Verificamos que se lo envió de vuelta al login
    verify(response).sendRedirect("/TallerApp/login.jsp");
  }

  @Test
  void deberia_RedirigirAlLoginDirectamente_Cuando_NoHaySesionActiva() throws Exception {
    // --- 1. ARRANGE ---
    // Simulamos que el usuario NO tiene sesión (por ejemplo, ya había cerrado sesión antes
    // o entró a la URL de logout por accidente)
    when(request.getSession(false)).thenReturn(null);
    when(request.getContextPath()).thenReturn("/TallerApp");

    // --- 2. ACT ---
    servlet.doGet(request, response);

    // --- 3. ASSERT ---
    // Como la sesión es null, el invalidate() no debió llamarse (si se llamara, daría NullPointerException).
    // Solo verificamos que la redirección ocurra sin errores.
    verify(response).sendRedirect("/TallerApp/login.jsp");
  }
}