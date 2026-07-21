package proyecto.interfaces.servlets;

import proyecto.interfaces.dao.UsuarioDAO;
import proyecto.interfaces.entities.Usuario;
import proyecto.interfaces.enums.RolUsuario;
import proyecto.interfaces.utils.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;


@WebServlet("/UsuariosController")
public class UsuarioServlet extends HttpServlet {
  private UsuarioDAO usuarioDAO;

  @Override
  public void init() throws ServletException {
    this.usuarioDAO = new UsuarioDAO();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");
    if (action == null) {
      action = "listar";
    }

    try {
      switch (action) {
        case "formularioRegisUsuario":
          mostrarFormulario(request, response, 0);
          break;
        case "editar":
          mostrarFormulario(request, response, Integer.parseInt(request.getParameter("idUsuario")));
          break;
        case "eliminar":
          eliminarUsuario(request, response);
          break;
        case "listar":
        default:
          listarUsuarios(request, response);
      }
    } catch (Exception e) {
      request.getSession().setAttribute("error", "Error interno: " + e.getMessage());
      response.sendRedirect(request.getContextPath() + "/UsuariosController?action=listar");
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");
    if ("guardar".equals(action)) {
      guardarUsuario(request, response);
    } else if ("actualizar".equals(action)) {
      actualizarUsuario(request, response);
    }
  }

  private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response, int idUsuario) throws Exception {
    Usuario usuario = (idUsuario == 0) ? new Usuario() : usuarioDAO.getById(idUsuario);

    if (usuario == null) {
      throw new Exception("Usuario con ID " + idUsuario + " no encontrado.");
    }

    String titulo = (idUsuario == 0) ? "Registrar Nuevo Usuario" : "Editar Usuario: " + usuario.getCorreoElectronico();

    request.setAttribute("titulo", titulo);
    request.setAttribute("usuario", usuario);
    request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp").forward(request, response);
  }

  private void guardarUsuario(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String correoElectronico = request.getParameter("correoElectronico");
    String password = request.getParameter("password");

    // USO DE STREAMS: Validación limpia y declarativa usando anyMatch
    boolean hayCamposVacios = Stream.of(correoElectronico, password, request.getParameter("nombre"), request.getParameter("apellido"))
        .anyMatch(val -> val == null || val.trim().isEmpty());

    if (hayCamposVacios) {
      request.setAttribute("error", "Todos los campos son obligatorios para el registro.");
      request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp").forward(request, response);
      return;
    }

    try {
      Usuario nuevoUsuario = new Usuario();
      mapearDatosRequestAUsuario(request, nuevoUsuario); // Modularización
      nuevoUsuario.setPassword(PasswordUtil.hashPassword(password));

      usuarioDAO.insert(nuevoUsuario);

      request.getSession().setAttribute("success", "Usuario '" + correoElectronico + "' registrado exitosamente.");
      response.sendRedirect(request.getContextPath() + "/UsuariosController?action=listar");

    } catch (IllegalArgumentException e) {
      request.setAttribute("error", "Error en el rol seleccionado.");
      request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp").forward(request, response);
    } catch (Exception e) {
      request.setAttribute("error", "Error al guardar el usuario: " + e.getMessage());
      request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp").forward(request, response);
    }
  }

  private void actualizarUsuario(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));

    try {
      Usuario usuarioAActualizar = usuarioDAO.getById(idUsuario);
      if (usuarioAActualizar == null) {
        throw new Exception("Usuario no encontrado para actualizar.");
      }

      // Reutilizamos el método de mapeo para mantener el código DRY (Don't Repeat Yourself)
      mapearDatosRequestAUsuario(request, usuarioAActualizar);

      String password = request.getParameter("password");
      if (password != null && !password.trim().isEmpty()) {
        usuarioAActualizar.setPassword(PasswordUtil.hashPassword(password));
      }

      usuarioDAO.update(usuarioAActualizar);

      request.getSession().setAttribute("success", "Usuario '" + usuarioAActualizar.getCorreoElectronico() + "' actualizado exitosamente.");
      response.sendRedirect(request.getContextPath() + "/UsuariosController?action=listar");

    } catch (Exception e) {
      request.setAttribute("error", "Error al actualizar el usuario: " + e.getMessage());
      try {
        request.setAttribute("usuario", usuarioDAO.getById(idUsuario));
      } catch(Exception ex) {}
      request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp").forward(request, response);
    }
  }

  private void listarUsuarios(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String busqueda = request.getParameter("busquedaApellido");
    List<Usuario> listaUsuarios;

    if (busqueda != null && !busqueda.trim().isEmpty()) {
      listaUsuarios = usuarioDAO.getByApellido(busqueda);
      request.setAttribute("busquedaActual", busqueda);
    } else {
      listaUsuarios = usuarioDAO.getAll();
    }

    request.setAttribute("listaUsuarios", listaUsuarios);
    request.getRequestDispatcher("/vistas/admin/listadoUsuarios.jsp").forward(request, response);
  }

  private void eliminarUsuario(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    try {
      int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
      Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("usuarioLogueado");

      if (usuarioLogueado != null && usuarioLogueado.getIdUsuario() == idUsuario) {
        request.getSession().setAttribute("error", "Error: No puedes eliminar tu propia cuenta.");
      } else {
        usuarioDAO.delete(idUsuario);
        request.getSession().setAttribute("success", "Usuario eliminado exitosamente.");
      }
    } catch (Exception e) {
      request.getSession().setAttribute("error", "Error al eliminar: " + e.getMessage());
    }
    response.sendRedirect(request.getContextPath() + "/UsuariosController?action=listar");
  }

  // --- MÉTODOS AUXILIARES ---

  /**
   * Extrae los datos repetitivos del HttpServletRequest y los inyecta en el objeto Usuario.
   */
  private void mapearDatosRequestAUsuario(HttpServletRequest request, Usuario usuario) {
    usuario.setCorreoElectronico(request.getParameter("correoElectronico"));
    usuario.setNombre(request.getParameter("nombre"));
    usuario.setApellido(request.getParameter("apellido"));
    usuario.setRol(RolUsuario.valueOf(request.getParameter("rol")));
  }
}