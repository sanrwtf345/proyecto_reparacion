package proyecto.interfaces.servlets;

import proyecto.interfaces.dao.UsuariosDAO;
import proyecto.interfaces.entities.Usuarios;
import proyecto.interfaces.enums.RolUsuario; // Necesario para guardar el rol

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Servlet que maneja las operaciones CRUD (Listar, Eliminar, Guardar) para el administrador.
 */
@WebServlet("/UsuariosController") // Usamos este mapeo para que la URL en el menú sea consistente
public class UsuarioServlet extends HttpServlet {
  private UsuariosDAO usuarioDAO;

  @Override
  public void init() throws ServletException {
    // Inicializa el DAO para la conexión a la base de datos
    this.usuarioDAO = new UsuariosDAO();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");
    if (action == null) {
      action = "listar"; // Acción por defecto
    }

    try {
      switch (action) {
        case "listar":
          listarUsuarios(request, response);
          break;
        case "eliminar":
          eliminarUsuario(request, response);
          break;
        case "formularioRegisUsuario": // <-- CORRECCIÓN: Coincide con el parámetro del menú
          request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp").forward(request, response);
          break;
        default:
          listarUsuarios(request, response);
      }
    } catch (Exception e) {
      request.setAttribute("error", "Error interno del servidor: " + e.getMessage());
      listarUsuarios(request, response);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");

    if (action != null && action.equals("guardar")) {
      guardarUsuario(request, response);
    }
  }

  private void guardarUsuario(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    // 1. Obtener parámetros del formulario
    String nombreUsuario = request.getParameter("nombreUsuario");
    String password = request.getParameter("password");
    String nombre = request.getParameter("nombre");
    String apellido = request.getParameter("apellido");
    String rolStr = request.getParameter("rol");

    // 2. Validación y lógica de guardado
    if (nombreUsuario == null || nombreUsuario.trim().isEmpty() || password == null || password.trim().isEmpty()) {
      request.setAttribute("error", "Los campos de usuario y contraseña son obligatorios.");
      // Volvemos al formulario para mostrar el error
      request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp").forward(request, response);
      return;
    }

    try {
      Usuarios nuevoUsuario = new Usuarios();
      nuevoUsuario.setNombreUsuario(nombreUsuario);
      nuevoUsuario.setPassword(password); // Recordar hashear la contraseña en producción real
      nuevoUsuario.setNombre(nombre);
      nuevoUsuario.setApellido(apellido);

      // Convertir String a Enum
      nuevoUsuario.setRol(RolUsuario.valueOf(rolStr));

      // Guardar en la base de datos
      usuarioDAO.insert(nuevoUsuario);

      // Redirigir al listado con mensaje de éxito
      request.getSession().setAttribute("success", "Usuario '" + nombreUsuario + "' registrado exitosamente.");
      response.sendRedirect(request.getContextPath() + "/UsuariosController?action=listar");

    } catch (IllegalArgumentException e) {
      // Error si el rol no existe o el enum falla
      request.setAttribute("error", "Error en el rol seleccionado: " + rolStr);
      request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp").forward(request, response);
    } catch (Exception e) {
      // Error de BD (ej. nombre de usuario duplicado, si la BD lo maneja)
      request.setAttribute("error", "Error al guardar el usuario: " + e.getMessage());
      request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp").forward(request, response);
    }
  }


  private void listarUsuarios(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    List<Usuarios> listaUsuarios = usuarioDAO.getAll();
    request.setAttribute("listaUsuarios", listaUsuarios);

    // forward a la vista ListadoUsuarios.jsp
    request.getRequestDispatcher("/vistas/admin/listadoUsuarios.jsp").forward(request, response);
  }

  private void eliminarUsuario(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    try {
      int idUsuario = Integer.parseInt(request.getParameter("id"));

      // No permite al admin eliminar su propia cuenta
      Usuarios usuarioLogueado = (Usuarios) request.getSession().getAttribute("usuarioLogueado");

      if (usuarioLogueado != null && usuarioLogueado.getIdUsuario() == idUsuario) {
        request.getSession().setAttribute("error", "Error: No puedes eliminar tu propia cuenta.");
      } else {
        usuarioDAO.delete(idUsuario);
        request.getSession().setAttribute("success", "Usuario eliminado exitosamente.");
      }
    } catch (NumberFormatException e) {
      request.getSession().setAttribute("error", "Error: ID de usuario inválido.");
    } catch (Exception e) {
      request.getSession().setAttribute("error", "Error al eliminar: " + e.getMessage());
    }

    // Redirigir de vuelta al listado para recargar la página y evitar doble acción
    response.sendRedirect(request.getContextPath() + "/UsuariosController?action=listar");
  }
}
