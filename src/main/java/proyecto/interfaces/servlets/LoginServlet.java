package proyecto.interfaces.servlets;

import proyecto.interfaces.dao.UsuariosDAO;
import proyecto.interfaces.entities.Usuarios;
import proyecto.interfaces.enums.RolUsuario;
import proyecto.interfaces.utils.PasswordUtil; // <--- PASO 1: IMPORTAR LA UTILIDAD

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

  private UsuariosDAO usuarioDAO;

  @Override
  public void init() throws ServletException {
    // Inicializa el DAO
    this.usuarioDAO = new UsuariosDAO();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // 1. Obtener datos del formulario
    String correoElectronico = request.getParameter("correoElectronico");
    String password = request.getParameter("password"); // Contraseña en TEXTO PLANO

    Usuarios usuario = null;

    try {
      // 2. Llama al DAO para buscar el usuario por correo
      // (En la BD, la contraseña está como HASH)
      usuario = usuarioDAO.getByCorreoElectronico(correoElectronico);

      // --- PASO 2: VERIFICAR LA CONTRASEÑA CON BCRYPT ---
      // (Implementación de la página 15 del PDF )
      // 3. Verifica la Contraseña
      if (usuario != null && PasswordUtil.verifyPassword(password, usuario.getPassword())) {
        // --- Login Exitoso ---
        HttpSession session = request.getSession();

        // 4. Guardar el usuario completo en la sesión
        session.setAttribute("usuarioLogueado", usuario);

        // 5. Redirección basada en el Rol (diferenciación de menú)
        if (usuario.getRol() == RolUsuario.ADMIN) {
          response.sendRedirect(request.getContextPath() + "/vistas/admin/menuAdmin.jsp");
        } else if (usuario.getRol() == RolUsuario.TECNICO) {
          response.sendRedirect(request.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");
        } else {
          // Rol desconocido
          response.sendError(HttpServletResponse.SC_FORBIDDEN, "Rol de usuario no reconocido.");
        }

      } else {
        // --- Login Fallido ---
        // (usuario == null O la contraseña BCrypt no coincidió)
        request.setAttribute("error", "Credenciales incorrectas o usuario no encontrado.");
        request.getRequestDispatcher("/login.jsp").forward(request, response);
      }

    } catch (Exception e) {
      // Manejo de error general (BD, etc.)
      request.setAttribute("error", "Ocurrió un error en el servidor: " + e.getMessage());
      request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
  }
}


