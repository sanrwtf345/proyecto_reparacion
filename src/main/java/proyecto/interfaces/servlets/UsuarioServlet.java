package proyecto.interfaces.servlets;

import proyecto.interfaces.dao.UsuariosDAO;
import proyecto.interfaces.entities.Usuarios;
import proyecto.interfaces.enums.RolUsuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Servlet que maneja las operaciones CRUD (Listar, Eliminar, Guardar/Editar) para el administrador.
 */
@WebServlet("/UsuariosController")
public class UsuarioServlet extends HttpServlet {
  private UsuariosDAO usuarioDAO;

  @Override
  public void init() throws ServletException {
    this.usuarioDAO = new UsuariosDAO();
  }

  // =========================================================================
  //                             MÉTODOS GET
  // =========================================================================

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");
    if (action == null) {
      action = "listar";
    }

    try {
      switch (action) {
        case "listar":
          listarUsuarios(request, response);
          break;
        case "eliminar":
          eliminarUsuario(request, response);
          break;
        case "formularioRegisUsuario":
          // Muestra un formulario vacío para nuevo registro
          mostrarFormulario(request, response);
          break;
        case "editar": // Lógica para cargar datos para edición
          mostrarFormularioEdicion(request, response);
          break;
        default:
          listarUsuarios(request, response);
      }
    } catch (Exception e) {
      request.setAttribute("error", "Error interno del servidor: " + e.getMessage());
      listarUsuarios(request, response);
    }
  }

  private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String idUsuarioStr = request.getParameter("idUsuario");

    try {
      int idUsuario = Integer.parseInt(idUsuarioStr);
      Usuarios usuario = usuarioDAO.getById(idUsuario);

      if (usuario != null) {
        // Se pasa el objeto 'usuario' al JSP para precargar los campos
        request.setAttribute("usuario", usuario);
        mostrarFormulario(request, response);
      } else {
        request.getSession().setAttribute("error", "Usuario no encontrado con ID: " + idUsuarioStr);
        response.sendRedirect(request.getContextPath() + "/UsuariosController?action=listar");
      }
    } catch (NumberFormatException e) {
      request.getSession().setAttribute("error", "Error: ID de usuario inválido.");
      response.sendRedirect(request.getContextPath() + "/UsuariosController?action=listar");
    }
  }

  private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp").forward(request, response);
  }

  // =========================================================================
  //                             MÉTODOS POST
  // =========================================================================

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");

    if (action != null && action.equals("guardar")) {
      guardarUsuario(request, response);
    } else {
      doGet(request, response);
    }
  }

  /**
   * Maneja tanto la inserción de un nuevo usuario como la actualización de uno existente.
   */
  private void guardarUsuario(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    // 1. Declaración de variables fuera del try (SOLUCIÓN AL ERROR DE ÁMBITO)
    String idUsuarioStr = request.getParameter("idUsuario");
    Usuarios usuario = null; // Se inicializa a null, luego se crea o se carga
    String rolStr = null; // Se inicializa a null


    boolean esEdicion = (idUsuarioStr != null && !idUsuarioStr.isEmpty());
    String mensajeExito = "";

    try {
      if (esEdicion) {
        int idUsuario = Integer.parseInt(idUsuarioStr);
        usuario = usuarioDAO.getById(idUsuario);
        if (usuario == null) {
          throw new Exception("Usuario a editar no encontrado.");
        }
        mensajeExito = "Usuario N° " + idUsuario + " actualizado exitosamente.";
      } else {
        usuario = new Usuarios();
        mensajeExito = "Usuario registrado exitosamente.";
      }

      // 2. Obtener y setear los datos comunes
      String nombreUsuario = request.getParameter("nombreUsuario");
      String password = request.getParameter("password");
      String nombre = request.getParameter("nombre");
      String apellido = request.getParameter("apellido");

      // Asignación de rolStr (dentro del try, pero declarado afuera)
      rolStr = request.getParameter("rol");

      // Validación básica
      if (nombreUsuario == null || nombreUsuario.trim().isEmpty() || rolStr == null || rolStr.trim().isEmpty()) {
        request.setAttribute("error", "El nombre de usuario y el rol son obligatorios.");
        request.setAttribute("usuario", usuario);
        mostrarFormulario(request, response);
        return;
      }

      // 3. Setear los datos en el objeto
      usuario.setNombreUsuario(nombreUsuario);
      usuario.setNombre(nombre);
      usuario.setApellido(apellido);

      // Esta línea lanza IllegalArgumentException si rolStr no coincide con un Enum
      usuario.setRol(RolUsuario.valueOf(rolStr));

      // 4. Lógica de Contraseña
      if (password != null && !password.trim().isEmpty()) {
        usuario.setPassword(password);
      } else if (!esEdicion) {
        request.setAttribute("error", "La contraseña es obligatoria para nuevos registros.");
        request.setAttribute("usuario", usuario);
        mostrarFormulario(request, response);
        return;
      }

      // 5. Ejecutar la operación DAO
      if (esEdicion) {
        usuarioDAO.update(usuario);
      } else {
        usuarioDAO.insert(usuario);
      }

      // 6. Redirección
      request.getSession().setAttribute("success", mensajeExito);
      response.sendRedirect(request.getContextPath() + "/UsuariosController?action=listar");

    } catch (NumberFormatException e) {
      // Maneja error si el ID de edición no es un número
      request.setAttribute("error", "Error en formato de ID.");
      request.setAttribute("usuario", usuario);
      mostrarFormulario(request, response);
    } catch (IllegalArgumentException e) {
      // Maneja error si el rolStr no coincide con un valor de RolUsuario
      request.setAttribute("error", "Error en el rol seleccionado. Valor proporcionado: " + rolStr);
      request.setAttribute("usuario", usuario);
      mostrarFormulario(request, response);
    } catch (Exception e) {
      // Maneja errores de BD o errores no esperados
      request.setAttribute("error", "Error al guardar el usuario: " + e.getMessage());
      request.setAttribute("usuario", usuario);
      mostrarFormulario(request, response);
    }
  }


  // =========================================================================
  //                         MÉTODOS DE LISTADO/ELIMINACIÓN
  // =========================================================================

  private void listarUsuarios(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    List<Usuarios> listaUsuarios = usuarioDAO.getAll();
    request.setAttribute("listaUsuarios", listaUsuarios);
    request.getRequestDispatcher("/vistas/admin/listadoUsuarios.jsp").forward(request, response);
  }

  private void eliminarUsuario(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    try {
      // Usamos idUsuario consistente con la URL del JSP
      int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));

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

    response.sendRedirect(request.getContextPath() + "/UsuariosController?action=listar");
  }
}
