package proyecto.interfaces.servlets;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // 1. Verificar si existe una sesión activa y, si existe, invalidarla.
    // Esto elimina el atributo "usuarioLogueado".
    if (request.getSession(false) != null) {
      request.getSession(false).invalidate();
    }

    // 2. Redirigir al usuario a la página de login
    response.sendRedirect(request.getContextPath() + "/login.jsp");
  }
}