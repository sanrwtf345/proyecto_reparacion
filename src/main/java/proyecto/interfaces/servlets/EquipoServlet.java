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
        response.sendRedirect(request.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");
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

        // --- PASO 2: AÑADIMOS LA ACCIÓN PARA MOSTRAR EL FORMULARIO DE EDICIÓN ---
        case "mostrarEditarEquipo":
          mostrarFormularioEditarEquipo(request, response);
          break;

        default:
          response.sendRedirect(request.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");
      }

    } catch (Exception e) {
      request.getSession().setAttribute("error", "Error en la operación de Equipo: " + e.getMessage());
      response.sendRedirect(request.getContextPath() + "/ClienteController?action=listar");
    }
  }


  // ... (listarEquiposPorCliente sin cambios) ...
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

  // ... (eliminarEquipo sin cambios) ...
  private void eliminarEquipo(HttpServletRequest request, HttpServletResponse response) throws Exception {
    int idEquipo = Integer.parseInt(request.getParameter("idEquipo"));
    int idCliente = Integer.parseInt(request.getParameter("idCliente"));
    List<Reparacion> reparaciones = reparacionDAO.getByEquipoId(idEquipo);
    for (Reparacion reparacion : reparaciones) {
      reparacionDAO.delete(reparacion.getIdReparacion());
    }
    equipoDAO.delete(idEquipo);
    request.getSession().setAttribute("success", "Equipo ID " + idEquipo + " y sus reparaciones asociadas fueron eliminados.");
    response.sendRedirect(request.getContextPath() + "/EquipoController?action=listarPorCliente&idCliente=" + idCliente);
  }

  // ... (mostrarFormularioAgregarEquipo sin cambios) ...
  private void mostrarFormularioAgregarEquipo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      List<Cliente> listaClientes = clienteDAO.getAll();
      request.setAttribute("listaClientes", listaClientes);
      request.getRequestDispatcher("/vistas/tecnico/agregarEquipo.jsp").forward(request, response);
    } catch (Exception e) {
      request.getSession().setAttribute("error", "Error al cargar la lista de clientes: " + e.getMessage());
      response.sendRedirect(request.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");
    }
  }

  // --- NUEVO MÉTODO (Paso 2 del plan) ---
  /**
   * Carga un equipo por ID y lo muestra en el formulario de edición.
   */
  private void mostrarFormularioEditarEquipo(HttpServletRequest request, HttpServletResponse response) throws Exception {
    // 1. Obtener el ID del equipo a editar
    int idEquipo = Integer.parseInt(request.getParameter("idEquipo"));

    // 2. Buscar el equipo en la BD
    Equipo equipo = equipoDAO.getById(idEquipo);
    if (equipo == null) {
      throw new Exception("Equipo con ID " + idEquipo + " no encontrado.");
    }

    // 3. (Importante) Cargar el objeto Cliente completo
    // Tu equipoDAO.getById() solo trae un placeholder del cliente (con el ID).
    // Necesitamos cargar el objeto Cliente completo para mostrar el nombre en el JSP.
    Cliente cliente = clienteDAO.getById(equipo.getCliente().getIdCliente());
    equipo.setCliente(cliente); // Reemplazamos el placeholder por el objeto completo

    // 4. Enviar el equipo al JSP
    request.setAttribute("equipo", equipo);

    // 5. Forward al nuevo formulario que creamos
    request.getRequestDispatcher("/vistas/tecnico/formularioEditarEquipo.jsp").forward(request, response);
  }


  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");

    if ("guardarNuevoEquipo".equals(action)) {
      guardarNuevoEquipo(request, response);
    }
    // --- PASO 3: AÑADIMOS LA ACCIÓN PARA ACTUALIZAR EL EQUIPO ---
    else if ("actualizarEquipo".equals(action)) {
      actualizarEquipo(request, response);
    }
    else {
      response.sendRedirect(request.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");
    }
  }

  // --- NUEVO MÉTODO (Paso 3 del plan) ---
  /**
   * Procesa el formulario de edición y actualiza el equipo en la BD.
   */
  private void actualizarEquipo(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String idClienteStr = request.getParameter("idCliente"); // Lo necesitamos para la redirección

    try {
      // 1. Obtener todos los parámetros del formulario
      int idEquipo = Integer.parseInt(request.getParameter("idEquipo"));
      int idCliente = Integer.parseInt(idClienteStr);

      String tipoEquipo = request.getParameter("tipoEquipo");
      String marca = request.getParameter("marca");
      String modelo = request.getParameter("modelo");
      String numSerie = request.getParameter("numSerie");
      String problemaReportado = request.getParameter("problemaReportado");

      // 2. Cargar el objeto Equipo existente desde la BD
      // (No usamos el de la sesión para evitar datos desactualizados)
      Equipo equipoAActualizar = equipoDAO.getById(idEquipo);
      if (equipoAActualizar == null) {
        throw new Exception("Error: El equipo que intenta actualizar ya no existe.");
      }

      // 3. Aplicar los nuevos valores
      equipoAActualizar.setTipoEquipo(tipoEquipo);
      equipoAActualizar.setMarca(marca);
      equipoAActualizar.setModelo(modelo);
      equipoAActualizar.setNumeroSerie(numSerie);
      equipoAActualizar.setProblemaReportado(problemaReportado);

      // 4. Guardar en la BD
      equipoDAO.update(equipoAActualizar);

      // 5. Redirigir a la lista de equipos de ESE cliente con mensaje de éxito
      request.getSession().setAttribute("success", "Equipo ID " + idEquipo + " actualizado exitosamente.");
      response.sendRedirect(request.getContextPath() + "/EquipoController?action=listarPorCliente&idCliente=" + idCliente);

    } catch (Exception e) {
      // En caso de error, volver a la lista de clientes (o al formulario)
      request.getSession().setAttribute("error", "Error al actualizar el equipo: " + e.getMessage());
      response.sendRedirect(request.getContextPath() + "/EquipoController?action=listarPorCliente&idCliente=" + idClienteStr);
    }
  }


  // ... (guardarNuevoEquipo sin cambios) ...
  private void guardarNuevoEquipo(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    Usuarios tecnicoLogueado = (Usuarios) request.getSession().getAttribute("usuarioLogueado");
    Integer idUsuarioSesion = null;

    if (tecnicoLogueado != null) {
      idUsuarioSesion = tecnicoLogueado.getIdUsuario();
    }

    if (idUsuarioSesion == null || idUsuarioSesion <= 0) {
      request.setAttribute("error", "Debe iniciar sesión como técnico para registrar un equipo.");
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
        throw new Exception("Cliente no encontrado en la base de datos.");
      }

      Equipo nuevoEquipo = new Equipo();
      nuevoEquipo.setCliente(cliente);
      nuevoEquipo.setTipoEquipo(tipoEquipo);
      nuevoEquipo.setMarca(marca);
      nuevoEquipo.setModelo(modelo);
      nuevoEquipo.setNumeroSerie(numSerie);
      nuevoEquipo.setProblemaReportado(problemaReportado);

      equipoDAO.insert(nuevoEquipo);

      request.getSession().setAttribute("success", "Equipo '" + tipoEquipo + "' registrado exitosamente para el cliente: " + cliente.getNombre());
      response.sendRedirect(request.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");

    } catch (NumberFormatException e) {
      request.setAttribute("error", "Error de formato de ID. " + e.getMessage());
      mostrarFormularioAgregarEquipo(request, response);
    } catch (Exception e) {
      request.setAttribute("error", "Error al guardar el equipo: " + e.getMessage());
      mostrarFormularioAgregarEquipo(request, response);
    }
  }
}
