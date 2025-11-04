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

import java.io.IOException;
import java.util.List;


@WebServlet("/UsuariosController")
public class UsuarioServlet extends HttpServlet {
  private UsuariosDAO usuarioDAO;

  @Override
  public void init() throws ServletException {
    this.usuarioDAO = new UsuariosDAO();
  }

  // ... (El método doGet no cambia) ...
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
        case "formularioRegisUsuario":
          // Modo Creación (ID 0)
          mostrarFormulario(request, response, 0);
          break;
        case "editar":
          // CORRECCIÓN 1: Nueva acción para mostrar formulario de edición
          int idUsuarioEditar = Integer.parseInt(request.getParameter("idUsuario"));
          mostrarFormulario(request, response, idUsuarioEditar);
          break;
        case "eliminar":
          eliminarUsuario(request, response);
          break;
        default:
          listarUsuarios(request, response);
      }
    } catch (Exception e) {
      request.getSession().setAttribute("error", "Error interno del servidor en la operación: " + e.getMessage());
      response.sendRedirect(request.getContextPath() + "/UsuariosController?action=listar");
    }
  }


  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");

    if (action != null) {
      if (action.equals("guardar")) {
        guardarUsuario(request, response); // Nuevo usuario (INSERT)
      } else if (action.equals("actualizar")) {
        actualizarUsuario(request, response); // Usuario existente (UPDATE)
      }
    }
  }

  // ... (El método mostrarFormulario no cambia) ...
  private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response, int idUsuario) throws Exception {
    Usuarios usuario;
    if (idUsuario == 0) {
      // Modo Creación
      usuario = new Usuarios();
      request.setAttribute("titulo", "Registrar Nuevo Usuario");
    } else {
      // Modo Edición: Cargar datos
      usuario = usuarioDAO.getById(idUsuario);
      if (usuario == null) {
        throw new Exception("Usuario con ID " + idUsuario + " no encontrado.");
      }
      // <--- CAMBIO: Título del formulario
      request.setAttribute("titulo", "Editar Usuario: " + usuario.getCorreoElectronico());
    }

    request.setAttribute("usuario", usuario);
    request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp").forward(request, response);
  }


  private void guardarUsuario(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    // 1. Obtener parámetros del formulario
    String correoElectronico = request.getParameter("correoElectronico");
    String password = request.getParameter("password"); // Contraseña en TEXTO PLANO
    String nombre = request.getParameter("nombre");
    String apellido = request.getParameter("apellido");
    String rolStr = request.getParameter("rol");

    // 2. Validación
    if (correoElectronico == null || correoElectronico.trim().isEmpty() || password == null || password.trim().isEmpty()) {
      request.setAttribute("error", "Los campos de Correo Electrónico y Contraseña son obligatorios.");
      request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp").forward(request, response);
      return;
    }

    try {
      Usuarios nuevoUsuario = new Usuarios();
      nuevoUsuario.setCorreoElectronico(correoElectronico);
      nuevoUsuario.setNombre(nombre);
      nuevoUsuario.setApellido(apellido);
      nuevoUsuario.setRol(RolUsuario.valueOf(rolStr));

      // --- PASO 2: HASHEAR LA CONTRASEÑA ---
      // (Implementación de la página 15 del PDF )
      String passwordHasheada = PasswordUtil.hashPassword(password);
      nuevoUsuario.setPassword(passwordHasheada); // Guardamos el HASH, no el texto plano
      // -------------------------------------

      // Guardar en la base de datos
      usuarioDAO.insert(nuevoUsuario);

      request.getSession().setAttribute("success", "Usuario '" + correoElectronico + "' registrado exitosamente.");
      response.sendRedirect(request.getContextPath() + "/UsuariosController?action=listar");

    } catch (IllegalArgumentException e) {
      request.setAttribute("error", "Error en el rol seleccionado: " + rolStr);
      request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp").forward(request, response);
    } catch (Exception e) {
      request.setAttribute("error", "Error al guardar el usuario: " + e.getMessage());
      request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp").forward(request, response);
    }
  }

  private void actualizarUsuario(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      // 1. Obtener ID y nuevos parámetros
      int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
      String correoElectronico = request.getParameter("correoElectronico");
      String password = request.getParameter("password"); // Contraseña en TEXTO PLANO (o vacía)
      String nombre = request.getParameter("nombre");
      String apellido = request.getParameter("apellido");
      String rolStr = request.getParameter("rol");

      // 2. Cargar el objeto existente
      Usuarios usuarioAActualizar = usuarioDAO.getById(idUsuario);
      if (usuarioAActualizar == null) {
        throw new Exception("Usuario con ID " + idUsuario + " no encontrado para actualizar.");
      }

      // 3. Aplicar cambios
      usuarioAActualizar.setCorreoElectronico(correoElectronico);
      usuarioAActualizar.setNombre(nombre);
      usuarioAActualizar.setApellido(apellido);
      usuarioAActualizar.setRol(RolUsuario.valueOf(rolStr));

      // --- PASO 3: HASHEAR LA CONTRASEÑA (SÓLO SI SE CAMBIÓ) ---
      // (Implementación de la página 15 del PDF )
      if (password != null && !password.trim().isEmpty()) {
        // El usuario escribió una nueva contraseña en el formulario
        String passwordHasheada = PasswordUtil.hashPassword(password);
        usuarioAActualizar.setPassword(passwordHasheada);
      }
      // Si el campo 'password' vino vacío, no hacemos nada
      // y se conserva la contraseña (hash) anterior en la BD.
      // --------------------------------------------------------

      // 4. Actualizar en BD
      usuarioDAO.update(usuarioAActualizar);

      request.getSession().setAttribute("success", "Usuario '" + correoElectronico + "' actualizado exitosamente.");
      response.sendRedirect(request.getContextPath() + "/UsuariosController?action=listar");

    } catch (Exception e) {
      request.setAttribute("error", "Error al actualizar el usuario: " + e.getMessage());

      // Recargamos el formulario en caso de error
      try {
        int idUsuarioError = Integer.parseInt(request.getParameter("idUsuario"));
        Usuarios usuarioError = usuarioDAO.getById(idUsuarioError);
        request.setAttribute("usuario", usuarioError);
      } catch(Exception ex) {
        // Si falla la recarga, simplemente redirigimos
      }

      request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp").forward(request, response);
    }
  }


  // ... (El método listarUsuarios no cambia) ...
  private void listarUsuarios(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    List<Usuarios> listaUsuarios = usuarioDAO.getAll();
    request.setAttribute("listaUsuarios", listaUsuarios);

    // forward a la vista ListadoUsuarios.jsp
    request.getRequestDispatcher("/vistas/admin/listadoUsuarios.jsp").forward(request, response);
  }

  // ... (El método eliminarUsuario no cambia) ...
  private void eliminarUsuario(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    try {
      int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));

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
    } catch (RuntimeException e) { // Capturamos la RuntimeException lanzada por el DAO si el registro no existe
      request.getSession().setAttribute("error", "Error al eliminar: " + e.getMessage());
    } catch (Exception e) {
      request.getSession().setAttribute("error", "Error al eliminar: " + e.getMessage());
    }

    // Redirigir de vuelta al listado para recargar la página y evitar doble acción
    response.sendRedirect(request.getContextPath() + "/UsuariosController?action=listar");
  }
}
