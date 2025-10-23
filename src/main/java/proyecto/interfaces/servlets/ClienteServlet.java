package proyecto.interfaces.servlets;

import proyecto.interfaces.dao.ClienteDAO;
import proyecto.interfaces.dao.EquipoDAO;
import proyecto.interfaces.dao.ReparacionDAO;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Equipo;
import proyecto.interfaces.entities.Reparacion;
import proyecto.interfaces.entities.Usuarios;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Servlet para la gestión completa CRUD de clientes.
 * - Listar (GET)
 * - Crear (GET/POST)
 * - Editar (GET/POST)
 * - Eliminar (GET)
 */
@WebServlet("/ClienteController")
public class ClienteServlet extends HttpServlet {
  private ClienteDAO clienteDAO;
  private EquipoDAO equipoDAO;
  private ReparacionDAO reparacionDAO;

  @Override
  public void init() throws ServletException {
    // Inicialización de DAOs
    this.clienteDAO = new ClienteDAO();
    this.equipoDAO = new EquipoDAO();
    this.reparacionDAO = new ReparacionDAO();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");

    try {
      switch (action == null ? "listar" : action) {
        case "listar":
          listarClientes(request, response);
          break;
        case "crear":
          // Muestra el formulario vacío para crear un nuevo cliente
          mostrarFormulario(request, response, 0);
          break;
        case "editar":
          // Muestra el formulario precargado para editar
          int idClienteEditar = Integer.parseInt(request.getParameter("idCliente"));
          mostrarFormulario(request, response, idClienteEditar);
          break;
        case "eliminar":
          eliminarCliente(request, response);
          break;
        default:
          listarClientes(request, response);
          break;
      }
    } catch (Exception e) {
      request.getSession().setAttribute("error", "Error en la operación del cliente: " + e.getMessage());
      response.sendRedirect(request.getContextPath() + "/ClienteController?action=listar");
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");

    if ("actualizar".equals(action)) {
      actualizarCliente(request, response);
    } else if ("guardar".equals(action)) {
      guardarCliente(request, response); // Nuevo método para INSERT
    } else {
      response.sendRedirect(request.getContextPath() + "/ClienteController?action=listar");
    }
  }

  /**
   * Muestra el formulario para crear (idCliente = 0) o editar (idCliente > 0) un cliente.
   * Se ajustó la ruta del dispatcher a /vistas/tecnico/
   */
  private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response, int idCliente) throws Exception {
    Cliente cliente;
    if (idCliente == 0) {
      // Modo Creación: Objeto cliente vacío
      cliente = new Cliente();
    } else {
      // Modo Edición: Cargar datos del cliente
      cliente = clienteDAO.getById(idCliente);
      if (cliente == null) {
        throw new Exception("Cliente con ID " + idCliente + " no encontrado.");
      }
    }

    request.setAttribute("cliente", cliente);
    request.getRequestDispatcher("/vistas/tecnico/formularioCliente.jsp").forward(request, response);
  }

  // --- Lógica de CREATE (Nuevo) ---
  private void guardarCliente(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      // 1. Obtener parámetros (no se necesita ID, es autoincremental)
      String nombre = request.getParameter("nombre");
      String apellido = request.getParameter("apellido");
      String telefono = request.getParameter("telefono");
      String email = request.getParameter("email");

      // Obtener el Usuario logueado de la sesión y verificar
      Usuarios usuarioLogueado = (Usuarios) request.getSession().getAttribute("usuarioLogueado");

      if (usuarioLogueado == null) {
        throw new Exception("Error al obtener ID de usuario para insertar cliente. No hay un usuario logueado en la sesión.");
      }

      // 3. Crear objeto Cliente
      Cliente nuevoCliente = new Cliente();
      nuevoCliente.setNombre(nombre);
      nuevoCliente.setApellido(apellido);
      nuevoCliente.setTelefono(telefono);
      nuevoCliente.setEmail(email);
      nuevoCliente.setUsuario(usuarioLogueado); // ASIGNACIÓN CLAVE

      // 4. Insertar
      clienteDAO.insert(nuevoCliente);

      request.getSession().setAttribute("success", "Cliente " + nombre + " registrado con éxito.");
      response.sendRedirect(request.getContextPath() + "/ClienteController?action=listar");

    } catch (Exception e) {
      request.setAttribute("error", "Error al registrar el cliente: " + e.getMessage());

      // Recargar el formulario con los datos ingresados para evitar pérdida
      Cliente clienteError = new Cliente();
      clienteError.setNombre(request.getParameter("nombre"));
      clienteError.setApellido(request.getParameter("apellido"));
      clienteError.setTelefono(request.getParameter("telefono"));
      clienteError.setEmail(request.getParameter("email"));
      request.setAttribute("cliente", clienteError);

      request.getRequestDispatcher("/vistas/tecnico/formularioCliente.jsp").forward(request, response);
    }
  }

  // --- Lógica de UPDATE (Edición) ---
  private void actualizarCliente(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      // 1. Obtener parámetros (incluyendo ID)
      int idCliente = Integer.parseInt(request.getParameter("idCliente"));
      String nombre = request.getParameter("nombre");
      String apellido = request.getParameter("apellido");
      String telefono = request.getParameter("telefono");
      String email = request.getParameter("email");

      //Cargar el cliente existente primero para mantener el objeto Usuario asociado
      Cliente cliente = clienteDAO.getById(idCliente);
      if (cliente == null) {
        throw new Exception("Error al actualizar: Cliente con ID " + idCliente + " no encontrado.");
      }

      // 3. Actualizar SOLO los campos que vienen del formulario (nombre, apellido, etc.)
      cliente.setNombre(nombre);
      cliente.setApellido(apellido);
      cliente.setTelefono(telefono);
      cliente.setEmail(email);

      // 4. Guardar cambios
      clienteDAO.update(cliente);

      request.getSession().setAttribute("success", "Cliente " + nombre + " actualizado con éxito.");
      response.sendRedirect(request.getContextPath() + "/ClienteController?action=listar");

    } catch (Exception e) {
      request.setAttribute("error", "Error al actualizar el cliente: " + e.getMessage());

      // Recargamos el formulario en caso de error
      try {
        // Si falla la actualización, volvemos al formulario con los datos que el usuario intentó enviar
        Cliente clienteError = new Cliente();
        clienteError.setIdCliente(Integer.parseInt(request.getParameter("idCliente")));
        clienteError.setNombre(request.getParameter("nombre"));
        clienteError.setApellido(request.getParameter("apellido"));
        clienteError.setTelefono(request.getParameter("telefono"));
        clienteError.setEmail(request.getParameter("email"));
        request.setAttribute("cliente", clienteError);
      } catch (NumberFormatException ex) { /* Ignorar si falla el ID */ }

      request.getRequestDispatcher("/vistas/tecnico/formularioCliente.jsp").forward(request, response);
    }
  }


  private void listarClientes(HttpServletRequest request, HttpServletResponse response) throws Exception {
    List<Cliente> listaClientes = clienteDAO.getAll();
    request.setAttribute("listaClientes", listaClientes);
    request.getRequestDispatcher("/vistas/tecnico/listaClientes.jsp").forward(request, response);
  }

  // --- Lógica de DELETE (Eliminar) ---
  private void eliminarCliente(HttpServletRequest request, HttpServletResponse response) throws Exception {
    int idCliente = Integer.parseInt(request.getParameter("idCliente"));

    // 1. Buscar equipos y reparaciones asociadas para eliminación en cascada
    List<Equipo> equipos = equipoDAO.getByClienteId(idCliente);

    for (Equipo equipo : equipos) {
      List<Reparacion> reparaciones = reparacionDAO.getByEquipoId(equipo.getIdEquipo());

      // 2. Eliminar todas las reparaciones de cada equipo
      for (Reparacion reparacion : reparaciones) {
        reparacionDAO.delete(reparacion.getIdReparacion());
      }

      // 3. Eliminar el equipo
      equipoDAO.delete(equipo.getIdEquipo());
    }

    // 4. Eliminar el cliente
    clienteDAO.delete(idCliente);

    request.getSession().setAttribute("success", "Cliente ID " + idCliente + " y todos sus datos asociados fueron eliminados correctamente.");
    response.sendRedirect(request.getContextPath() + "/ClienteController?action=listar");
  }
}
