package proyecto.interfaces.servlets;

import proyecto.interfaces.dao.ClienteDAO;
import proyecto.interfaces.dao.EquipoDAO;
import proyecto.interfaces.dao.ReparacionDAO;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Equipo;
import proyecto.interfaces.entities.Reparacion;
import proyecto.interfaces.entities.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

@WebServlet("/ClienteController")
public class ClienteServlet extends HttpServlet {
  private ClienteDAO clienteDAO;
  private EquipoDAO equipoDAO;
  private ReparacionDAO reparacionDAO;

  @Override
  public void init() throws ServletException {
    this.clienteDAO = new ClienteDAO();
    this.equipoDAO = new EquipoDAO();
    this.reparacionDAO = new ReparacionDAO();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");

    try {
      switch (action == null ? "listar" : action) {
        case "crear":
          mostrarFormulario(request, response, 0);
          break;
        case "editar":
          mostrarFormulario(request, response, Integer.parseInt(request.getParameter("idCliente")));
          break;
        case "eliminar":
          eliminarCliente(request, response);
          break;
        case "listar":
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
      guardarCliente(request, response);
    } else {
      response.sendRedirect(request.getContextPath() + "/ClienteController?action=listar");
    }
  }

  private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response, int idCliente) throws Exception {
    Cliente cliente;
    if (idCliente == 0) {
      cliente = new Cliente();
    } else {
      cliente = clienteDAO.getById(idCliente);
      if (cliente == null) {
        throw new Exception("Cliente con ID " + idCliente + " no encontrado.");
      }
    }

    request.setAttribute("cliente", cliente);
    request.getRequestDispatcher("/vistas/tecnico/formularioCliente.jsp").forward(request, response);
  }

  private void guardarCliente(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      // Validación declarativa con Streams
      if (faltanDatosObligatorios(request)) {
        throw new IllegalArgumentException("El nombre y el apellido son obligatorios.");
      }

      Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("usuarioLogueado");
      if (usuarioLogueado == null) {
        throw new Exception("Error: No hay un usuario logueado en la sesión.");
      }

      Cliente nuevoCliente = new Cliente();
      mapearDatosRequestACliente(request, nuevoCliente); // Modularización
      nuevoCliente.setUsuario(usuarioLogueado);

      clienteDAO.insert(nuevoCliente);

      request.getSession().setAttribute("success", "Cliente " + nuevoCliente.getNombre() + " registrado con éxito.");
      response.sendRedirect(request.getContextPath() + "/ClienteController?action=listar");

    } catch (Exception e) {
      request.setAttribute("error", "Error al registrar el cliente: " + e.getMessage());

      Cliente clienteError = new Cliente();
      mapearDatosRequestACliente(request, clienteError);
      request.setAttribute("cliente", clienteError);
      request.getRequestDispatcher("/vistas/tecnico/formularioCliente.jsp").forward(request, response);
    }
  }

  private void actualizarCliente(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      if (faltanDatosObligatorios(request)) {
        throw new IllegalArgumentException("El nombre y el apellido son obligatorios.");
      }

      int idCliente = Integer.parseInt(request.getParameter("idCliente"));
      Cliente cliente = clienteDAO.getById(idCliente);

      if (cliente == null) {
        throw new Exception("Error al actualizar: Cliente con ID " + idCliente + " no encontrado.");
      }

      mapearDatosRequestACliente(request, cliente); // Modularización

      clienteDAO.update(cliente);

      request.getSession().setAttribute("success", "Cliente " + cliente.getNombre() + " actualizado con éxito.");
      response.sendRedirect(request.getContextPath() + "/ClienteController?action=listar");

    } catch (Exception e) {
      request.setAttribute("error", "Error al actualizar el cliente: " + e.getMessage());

      try {
        Cliente clienteError = new Cliente();
        clienteError.setIdCliente(Integer.parseInt(request.getParameter("idCliente")));
        mapearDatosRequestACliente(request, clienteError);
        request.setAttribute("cliente", clienteError);
      } catch (NumberFormatException ex) {}

      request.getRequestDispatcher("/vistas/tecnico/formularioCliente.jsp").forward(request, response);
    }
  }

  private void listarClientes(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String busqueda = request.getParameter("busquedaApellido");
    List<Cliente> listaClientes;

    if (busqueda != null && !busqueda.trim().isEmpty()) {
      listaClientes = clienteDAO.getByApellido(busqueda);
      request.setAttribute("busquedaActual", busqueda);
    } else {
      listaClientes = clienteDAO.getAll();
    }

    request.setAttribute("listaClientes", listaClientes);
    request.getRequestDispatcher("/vistas/tecnico/listaClientes.jsp").forward(request, response);
  }

  private void eliminarCliente(HttpServletRequest request, HttpServletResponse response) throws Exception {
    int idCliente = Integer.parseInt(request.getParameter("idCliente"));
    List<Equipo> equipos = equipoDAO.getByClienteId(idCliente);

    // USO DE STREAMS: Eliminación en cascada usando forEach (Consumer)
    equipos.forEach(equipo -> {
      reparacionDAO.getByEquipoId(equipo.getIdEquipo())
          .forEach(reparacion -> reparacionDAO.delete(reparacion.getIdReparacion()));

      equipoDAO.delete(equipo.getIdEquipo());
    });

    clienteDAO.delete(idCliente);

    request.getSession().setAttribute("success", "Cliente ID " + idCliente + " y todos sus datos asociados fueron eliminados correctamente.");
    response.sendRedirect(request.getContextPath() + "/ClienteController?action=listar");
  }

  // --- MÉTODOS AUXILIARES ---

  /**
   * Extrae los datos del request y los setea en el cliente. Mantiene el código DRY.
   */
  private void mapearDatosRequestACliente(HttpServletRequest request, Cliente cliente) {
    cliente.setNombre(request.getParameter("nombre"));
    cliente.setApellido(request.getParameter("apellido"));
    cliente.setTelefono(request.getParameter("telefono"));
    cliente.setEmail(request.getParameter("email"));
  }

  /**
   * Valida usando Streams (anyMatch) si los campos clave están vacíos.
   * anyMatch detiene la evaluación apenas encuentra un 'true' (cortocircuito).
   */
  private boolean faltanDatosObligatorios(HttpServletRequest request) {
    return Stream.of(request.getParameter("nombre"), request.getParameter("apellido"))
        .anyMatch(val -> val == null || val.trim().isEmpty());
  }
}