package proyecto.interfaces.servlets;

import proyecto.interfaces.dao.ClienteDAO;
import proyecto.interfaces.dao.EquipoDAO;
import proyecto.interfaces.dao.ReparacionDAO;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Equipo;
import proyecto.interfaces.entities.Reparacion;
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

        // --- ACCIÓN PARA MOSTRAR EL FORMULARIO DE EDICIÓN (NUEVO) ---
        case "mostrarEditarEquipo":
          mostrarFormularioEditarEquipo(request, response);
          break;

        default:
          redirigirAlMenu(request, response);
      }

    } catch (Exception e) {
      request.getSession().setAttribute("error", "Error en la operación de Equipo: " + e.getMessage());
      response.sendRedirect(request.getContextPath() + "/ClienteController?action=listar");
    }
  }

  // --- MÉTODO PARA MOSTRAR EDICIÓN (NUEVO) ---
  private void mostrarFormularioEditarEquipo(HttpServletRequest request, HttpServletResponse response) throws Exception {
    // 1. Obtener ID
    int idEquipo = Integer.parseInt(request.getParameter("idEquipo"));

    // 2. Buscar el equipo
    Equipo equipo = equipoDAO.getById(idEquipo);
    if (equipo == null) {
      throw new Exception("Equipo con ID " + idEquipo + " no encontrado.");
    }

    // 3. Cargar datos del cliente completo (para mostrar el nombre en el JSP)
    Cliente cliente = clienteDAO.getById(equipo.getCliente().getIdCliente());
    equipo.setCliente(cliente);

    // 4. Enviar al formulario de edición
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

    // Borrado en cascada manual
    List<Reparacion> reparaciones = reparacionDAO.getByEquipoId(idEquipo);
    for (Reparacion reparacion : reparaciones) {
      reparacionDAO.delete(reparacion.getIdReparacion());
    }
    equipoDAO.delete(idEquipo);

    request.getSession().setAttribute("success", "Equipo ID " + idEquipo + " y sus reparaciones eliminados.");
    response.sendRedirect(request.getContextPath() + "/EquipoController?action=listarPorCliente&idCliente=" + idCliente);
  }

  private void mostrarFormularioAgregarEquipo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      List<Cliente> listaClientes = clienteDAO.getAll();
      request.setAttribute("listaClientes", listaClientes);
      // Apunta al formulario correcto (agregarEquipo.jsp)
      request.getRequestDispatcher("/vistas/tecnico/agregarEquipo.jsp").forward(request, response);
    } catch (Exception e) {
      request.getSession().setAttribute("error", "Error al cargar la lista de clientes: " + e.getMessage());
      redirigirAlMenu(request, response);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");

    if ("guardarNuevoEquipo".equals(action)) {
      guardarNuevoEquipo(request, response);
    }
    // --- ACCIÓN PARA ACTUALIZAR (NUEVO) ---
    else if ("actualizarEquipo".equals(action)) {
      actualizarEquipo(request, response);
    }
    else {
      redirigirAlMenu(request, response);
    }
  }

  // --- MÉTODO PARA ACTUALIZAR (NUEVO) ---
  private void actualizarEquipo(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String idClienteStr = request.getParameter("idCliente"); // Para redirigir de vuelta

    try {
      // 1. Obtener datos
      int idEquipo = Integer.parseInt(request.getParameter("idEquipo"));
      String tipoEquipo = request.getParameter("tipoEquipo");
      String marca = request.getParameter("marca");
      String modelo = request.getParameter("modelo");
      String numSerie = request.getParameter("numSerie");
      String problemaReportado = request.getParameter("problemaReportado");

      // 2. Validar existencia
      Equipo equipo = equipoDAO.getById(idEquipo);
      if (equipo == null) {
        throw new Exception("El equipo a editar no existe.");
      }

      // 3. Actualizar objeto
      equipo.setTipoEquipo(tipoEquipo);
      equipo.setMarca(marca);
      equipo.setModelo(modelo);
      equipo.setNumeroSerie(numSerie);
      equipo.setProblemaReportado(problemaReportado);

      // 4. Guardar en BD
      equipoDAO.update(equipo);

      request.getSession().setAttribute("success", "Equipo actualizado correctamente.");
      response.sendRedirect(request.getContextPath() + "/EquipoController?action=listarPorCliente&idCliente=" + idClienteStr);

    } catch (Exception e) {
      request.getSession().setAttribute("error", "Error al actualizar equipo: " + e.getMessage());
      // Si falla, intentamos volver a la lista del cliente
      if (idClienteStr != null) {
        response.sendRedirect(request.getContextPath() + "/EquipoController?action=listarPorCliente&idCliente=" + idClienteStr);
      } else {
        redirigirAlMenu(request, response);
      }
    }
  }

  private void guardarNuevoEquipo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    Usuarios tecnicoLogueado = (Usuarios) request.getSession().getAttribute("usuarioLogueado");
    Integer idUsuarioSesion = null;

    if (tecnicoLogueado != null) {
      idUsuarioSesion = tecnicoLogueado.getIdUsuario();
    }

    if (idUsuarioSesion == null || idUsuarioSesion <= 0) {
      request.setAttribute("error", "Debe iniciar sesión para registrar un equipo.");
      mostrarFormularioAgregarEquipo(request, response);
      return;
    }

    String idClienteStr = request.getParameter("idCliente");
    String tipoEquipo = request.getParameter("tipoEquipo");
    String marca = request.getParameter("marca");
    String modelo = request.getParameter("modelo");
    String numSerie = request.getParameter("numSerie");
    String problemaReportado = request.getParameter("problemaReportado");

    if (idClienteStr == null || idClienteStr.trim().isEmpty()) {
      request.setAttribute("error", "Datos incompletos: Debe seleccionar un cliente.");
      mostrarFormularioAgregarEquipo(request, response);
      return;
    }

    try {
      int idCliente = Integer.parseInt(idClienteStr);
      Cliente cliente = clienteDAO.getById(idCliente);
      if (cliente == null) {
        throw new Exception("Cliente no encontrado.");
      }

      Equipo nuevoEquipo = new Equipo();
      nuevoEquipo.setCliente(cliente);
      nuevoEquipo.setTipoEquipo(tipoEquipo);
      nuevoEquipo.setMarca(marca);
      nuevoEquipo.setModelo(modelo);
      nuevoEquipo.setNumeroSerie(numSerie);
      nuevoEquipo.setProblemaReportado(problemaReportado);

      equipoDAO.insert(nuevoEquipo);

      request.getSession().setAttribute("success", "Equipo '" + tipoEquipo + "' registrado exitosamente.");

      // Redirección inteligente
      redirigirAlMenu(request, response);

    } catch (NumberFormatException e) {
      request.setAttribute("error", "Error de formato de ID. " + e.getMessage());
      mostrarFormularioAgregarEquipo(request, response);
    } catch (Exception e) {
      request.setAttribute("error", "Error al guardar el equipo: " + e.getMessage());
      mostrarFormularioAgregarEquipo(request, response);
    }
  }

  // Método auxiliar para redirección inteligente según rol
  private void redirigirAlMenu(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Usuarios u = (Usuarios) request.getSession().getAttribute("usuarioLogueado");
    if (u != null && u.getRol() == RolUsuario.ADMIN) {
      response.sendRedirect(request.getContextPath() + "/vistas/admin/menuAdmin.jsp");
    } else {
      response.sendRedirect(request.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");
    }
  }
}
