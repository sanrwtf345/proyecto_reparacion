package proyecto.interfaces.servlets;

import proyecto.interfaces.dao.ClienteDAO;
import proyecto.interfaces.dao.EquipoDAO;
import proyecto.interfaces.dao.ReparacionDAO;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Equipo;
import proyecto.interfaces.entities.Reparacion;
import proyecto.interfaces.entities.Usuario;
import proyecto.interfaces.enums.RolUsuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

/**
 * Servlet para manejar el CRUD completo de Equipos
 * para un Cliente YA EXISTENTE en la base de datos.
 */
@WebServlet("/EquipoController")
public class EquipoServlet extends HttpServlet {
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
      if (action == null) {
        redirigirAlMenu(request, response);
        return;
      }

      switch(action) {
        case "mostrarAgregarEquipo":
          mostrarFormularioAgregarEquipo(request, response);
          break;
        case "listarPorCliente":
          listarEquiposPorCliente(request, response);
          break;
        case "eliminarEquipo":
          eliminarEquipo(request, response);
          break;
        case "mostrarEditarEquipo":
          mostrarFormularioEditarEquipo(request, response);
          break;
        case "verHistorial":
          verHistorial(request, response);
          break;
        default:
          redirigirAlMenu(request, response);
      }

    } catch (Exception e) {
      request.getSession().setAttribute("error", "Error en la operación de Equipo: " + e.getMessage());
      response.sendRedirect(request.getContextPath() + "/ClienteController?action=listar");
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");

    if ("guardarNuevoEquipo".equals(action)) {
      guardarNuevoEquipo(request, response);
    } else if ("actualizarEquipo".equals(action)) {
      actualizarEquipo(request, response);
    } else {
      redirigirAlMenu(request, response);
    }
  }

  private void mostrarFormularioEditarEquipo(HttpServletRequest request, HttpServletResponse response) throws Exception {
    int idEquipo = Integer.parseInt(request.getParameter("idEquipo"));

    Equipo equipo = equipoDAO.getById(idEquipo);
    if (equipo == null) {
      throw new Exception("Equipo con ID " + idEquipo + " no encontrado.");
    }

    Cliente cliente = clienteDAO.getById(equipo.getCliente().getIdCliente());
    equipo.setCliente(cliente);

    request.setAttribute("equipo", equipo);
    request.getRequestDispatcher("/vistas/tecnico/formularioEditarEquipo.jsp").forward(request, response);
  }

  private void listarEquiposPorCliente(HttpServletRequest request, HttpServletResponse response) throws Exception {
    int idCliente = Integer.parseInt(request.getParameter("idCliente"));
    Cliente cliente = clienteDAO.getById(idCliente);

    if (cliente == null) {
      throw new Exception("El cliente con ID " + idCliente + " no existe.");
    }

    List<Equipo> listaEquipos = equipoDAO.getByClienteId(idCliente);
    request.setAttribute("cliente", cliente);
    request.setAttribute("listaEquipos", listaEquipos);
    request.getRequestDispatcher("/vistas/tecnico/listaEquiposPorCliente.jsp").forward(request, response);
  }

  private void eliminarEquipo(HttpServletRequest request, HttpServletResponse response) throws Exception {
    int idEquipo = Integer.parseInt(request.getParameter("idEquipo"));
    int idCliente = Integer.parseInt(request.getParameter("idCliente"));

    // USO DE STREAMS: Eliminación en cascada mediante forEach (Consumer)
    reparacionDAO.getByEquipoId(idEquipo)
        .forEach(reparacion -> reparacionDAO.delete(reparacion.getIdReparacion()));

    equipoDAO.delete(idEquipo);

    request.getSession().setAttribute("success", "Equipo ID " + idEquipo + " y sus reparaciones eliminados.");
    response.sendRedirect(request.getContextPath() + "/EquipoController?action=listarPorCliente&idCliente=" + idCliente);
  }

  private void mostrarFormularioAgregarEquipo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      List<Cliente> listaClientes = clienteDAO.getAll();
      request.setAttribute("listaClientes", listaClientes);
      request.getRequestDispatcher("/vistas/tecnico/agregarEquipo.jsp").forward(request, response);
    } catch (Exception e) {
      request.getSession().setAttribute("error", "Error al cargar la lista de clientes: " + e.getMessage());
      redirigirAlMenu(request, response);
    }
  }

  private void actualizarEquipo(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String idClienteStr = request.getParameter("idCliente");

    try {
      if (faltanDatosObligatorios(request)) {
        throw new IllegalArgumentException("El tipo de equipo y el problema reportado son obligatorios.");
      }

      int idEquipo = Integer.parseInt(request.getParameter("idEquipo"));
      Equipo equipo = equipoDAO.getById(idEquipo);

      if (equipo == null) {
        throw new Exception("El equipo a editar no existe.");
      }

      mapearDatosRequestAEquipo(request, equipo); // Modularización

      equipoDAO.update(equipo);

      request.getSession().setAttribute("success", "Equipo actualizado correctamente.");
      response.sendRedirect(request.getContextPath() + "/EquipoController?action=listarPorCliente&idCliente=" + idClienteStr);

    } catch (Exception e) {
      request.getSession().setAttribute("error", "Error al actualizar equipo: " + e.getMessage());
      if (idClienteStr != null) {
        response.sendRedirect(request.getContextPath() + "/EquipoController?action=listarPorCliente&idCliente=" + idClienteStr);
      } else {
        redirigirAlMenu(request, response);
      }
    }
  }

  private void guardarNuevoEquipo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Usuario tecnicoLogueado = (Usuario) request.getSession().getAttribute("usuarioLogueado");

    if (tecnicoLogueado == null || tecnicoLogueado.getIdUsuario() <= 0) {
      request.setAttribute("error", "Debe iniciar sesión para registrar un equipo.");
      mostrarFormularioAgregarEquipo(request, response);
      return;
    }

    String idClienteStr = request.getParameter("idCliente");
    if (idClienteStr == null || idClienteStr.trim().isEmpty()) {
      request.setAttribute("error", "Datos incompletos: Debe seleccionar un cliente.");
      mostrarFormularioAgregarEquipo(request, response);
      return;
    }

    try {
      if (faltanDatosObligatorios(request)) {
        throw new IllegalArgumentException("El tipo de equipo y el problema reportado son obligatorios.");
      }

      int idCliente = Integer.parseInt(idClienteStr);
      Cliente cliente = clienteDAO.getById(idCliente);
      if (cliente == null) {
        throw new Exception("Cliente no encontrado.");
      }

      Equipo nuevoEquipo = new Equipo();
      nuevoEquipo.setCliente(cliente);
      mapearDatosRequestAEquipo(request, nuevoEquipo); // Modularización

      equipoDAO.insert(nuevoEquipo);

      request.getSession().setAttribute("success", "Equipo '" + nuevoEquipo.getTipoEquipo() + "' registrado exitosamente.");
      redirigirAlMenu(request, response);

    } catch (NumberFormatException e) {
      request.setAttribute("error", "Error de formato de ID. " + e.getMessage());
      mostrarFormularioAgregarEquipo(request, response);
    } catch (Exception e) {
      request.setAttribute("error", "Error al guardar el equipo: " + e.getMessage());
      mostrarFormularioAgregarEquipo(request, response);
    }
  }

  private void verHistorial(HttpServletRequest request, HttpServletResponse response) throws Exception {
    int idEquipo = Integer.parseInt(request.getParameter("idEquipo"));

    Equipo equipo = equipoDAO.getById(idEquipo);
    if (equipo == null) {
      throw new Exception("Equipo no encontrado");
    }

    Cliente cliente = clienteDAO.getById(equipo.getCliente().getIdCliente());
    equipo.setCliente(cliente);

    List<Reparacion> historial = reparacionDAO.getHistorialPorEquipo(idEquipo);

    request.setAttribute("equipo", equipo);
    request.setAttribute("historial", historial);
    request.getRequestDispatcher("/vistas/tecnico/historialReparaciones.jsp").forward(request, response);
  }

  private void redirigirAlMenu(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Usuario u = (Usuario) request.getSession().getAttribute("usuarioLogueado");
    if (u != null && u.getRol() == RolUsuario.ADMIN) {
      response.sendRedirect(request.getContextPath() + "/vistas/admin/menuAdmin.jsp");
    } else {
      response.sendRedirect(request.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");
    }
  }

  // --- MÉTODOS AUXILIARES ---

  /**
   * Extrae los datos repetitivos del request y los setea en el equipo.
   */
  private void mapearDatosRequestAEquipo(HttpServletRequest request, Equipo equipo) {
    equipo.setTipoEquipo(request.getParameter("tipoEquipo"));
    equipo.setMarca(request.getParameter("marca"));
    equipo.setModelo(request.getParameter("modelo"));
    equipo.setNumeroSerie(request.getParameter("numSerie"));
    equipo.setProblemaReportado(request.getParameter("problemaReportado"));
  }

  /**
   * Valida usando Streams si los campos clave están vacíos mediante anyMatch (cortocircuito).
   */
  private boolean faltanDatosObligatorios(HttpServletRequest request) {
    return Stream.of(request.getParameter("tipoEquipo"), request.getParameter("problemaReportado"))
        .anyMatch(val -> val == null || val.trim().isEmpty());
  }
}