package proyecto.interfaces.servlets;

import proyecto.interfaces.dao.ClienteDAO;
import proyecto.interfaces.dao.EquipoDAO;
import proyecto.interfaces.dao.ReparacionDAO;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Usuarios;
import proyecto.interfaces.entities.Equipo;
import proyecto.interfaces.entities.Reparacion;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Servlet que maneja el flujo de registro de Cliente (Paso 1) y Equipo/Orden (Paso 2).
 */
@WebServlet("/ClienteEquipoController")
public class ClienteServlet extends HttpServlet {
  private ClienteDAO clienteDAO;
  private EquipoDAO equipoDAO;
  private ReparacionDAO reparacionDAO;

  @Override
  public void init() throws ServletException {
    // Asegúrate de que tus implementaciones de DAO son las correctas
    this.clienteDAO = new ClienteDAO();
    this.equipoDAO = new EquipoDAO();
    this.reparacionDAO = new ReparacionDAO();
  }


  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");

    if ("mostrarEquipo".equals(action)) {
      mostrarFormularioEquipo(request, response);
    } else {
      // Acción por defecto: Muestra el formulario de registro de cliente (Paso 1)
      request.getRequestDispatcher("/vistas/tecnico/formularioCliente.jsp").forward(request, response);
    }
  }

  private void mostrarFormularioEquipo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String idClienteStr = request.getParameter("idCliente");

    if (idClienteStr == null || idClienteStr.trim().isEmpty()) {
      request.getSession().setAttribute("error", "Error de flujo: No se proporcionó el ID del cliente.");
      response.sendRedirect(request.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");
      return;
    }

    try {
      int idCliente = Integer.parseInt(idClienteStr);
      Cliente cliente = clienteDAO.getById(idCliente);

      if (cliente != null) {
        request.setAttribute("cliente", cliente);
        // Forward al JSP que registra el equipo
        request.getRequestDispatcher("/vistas/tecnico/formularioEquipo.jsp").forward(request, response);
      } else {
        request.getSession().setAttribute("error", "Cliente no encontrado con ID: " + idClienteStr);
        response.sendRedirect(request.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");
      }

    } catch (NumberFormatException e) {
      request.getSession().setAttribute("error", "Error: ID de cliente inválido.");
      response.sendRedirect(request.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");
    }
  }


  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");

    // Manejo de las dos acciones POST
    if ("guardarEquipoYOrden".equals(action)) {
      // Lógica para guardar el equipo y crear la orden (Paso 2)
      guardarEquipoYOrden(request, response);
    } else {
      // Lógica para guardar solo el cliente (Paso 1 - Por defecto de FormularioCliente.jsp)
      guardarClienteYContinuar(request, response);
    }
  }

  private void guardarClienteYContinuar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    // 1. Obtener datos y usuario logueado
    String nombre = request.getParameter("nombre");
    String apellido = request.getParameter("apellido");
    String telefono = request.getParameter("telefono");
    String email = request.getParameter("email");
    Usuarios usuarioLogueado = (Usuarios) request.getSession().getAttribute("usuarioLogueado");

    // Validación
    if (nombre == null || nombre.trim().isEmpty() || telefono == null || telefono.trim().isEmpty() || usuarioLogueado == null) {
      request.setAttribute("error", "Datos incompletos o sesión expirada.");
      request.getRequestDispatcher("/vistas/tecnico/formularioCliente.jsp").forward(request, response);
      return;
    }

    try {
      // 2. Crear y configurar Cliente
      Cliente nuevoCliente = new Cliente();
      nuevoCliente.setNombre(nombre);
      nuevoCliente.setApellido(apellido);
      nuevoCliente.setTelefono(telefono);
      nuevoCliente.setEmail(email);
      nuevoCliente.setUsuario(usuarioLogueado);

      // 3. Guardar el cliente usando el método CORREGIDO (devuelve ID)
      Integer idCliente = clienteDAO.insertWithIdReturn(nuevoCliente);

      if (idCliente != null) {
        // 4. Redirección al Paso 2
        String urlEquipo = request.getContextPath() + "/ClienteEquipoController?action=mostrarEquipo&idCliente=" + idCliente;
        response.sendRedirect(urlEquipo);
      } else {
        throw new Exception("La inserción falló y no se pudo obtener el ID del cliente.");
      }

    } catch (Exception e) {
      request.setAttribute("error", "Error al registrar el cliente: " + e.getMessage());
      request.getRequestDispatcher("/vistas/tecnico/formularioCliente.jsp").forward(request, response);
    }
  }

  private void guardarEquipoYOrden(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    // 1. Obtener parámetros
    String idClienteStr = request.getParameter("idCliente");
    String tipoStr = request.getParameter("tipo");
    String marca = request.getParameter("marca");
    String modelo = request.getParameter("modelo");
    String nroSerie = request.getParameter("nroSerie");
    String descripcionFalla = request.getParameter("descripcionFalla");

    if (idClienteStr == null || descripcionFalla == null || descripcionFalla.trim().isEmpty()) {
      request.setAttribute("error", "Faltan datos críticos para registrar la orden.");
      request.getRequestDispatcher("/vistas/tecnico/formularioEquipo.jsp").forward(request, response);
      return;
    }

    try {
      int idCliente = Integer.parseInt(idClienteStr);
      Cliente cliente = clienteDAO.getById(idCliente);
      Usuarios tecnico = (Usuarios) request.getSession().getAttribute("usuarioLogueado");

      if (cliente == null || tecnico == null) {
        throw new Exception("Error interno: Cliente o Técnico de la sesión no encontrado.");
      }

      // 2. Registrar el Equipo
      Equipo nuevoEquipo = new Equipo();
      nuevoEquipo.setCliente(cliente);
      nuevoEquipo.setTipoEquipo(tipoStr); // String, según tu DAO
      nuevoEquipo.setMarca(marca);
      nuevoEquipo.setModelo(modelo);
      nuevoEquipo.setNumeroSerie(nroSerie);
      nuevoEquipo.setProblemaReportado(descripcionFalla); // Mapeo a problema_reportado

      // Llamada al método corregido que devuelve el ID
      Integer idEquipo = equipoDAO.insertWithIdReturn(nuevoEquipo);

      if (idEquipo == null) {
        throw new Exception("Fallo al obtener el ID del Equipo.");
      }

      // 3. Registrar la Orden de Reparación
      Reparacion orden = new Reparacion();
      nuevoEquipo.setIdEquipo(idEquipo);

      orden.setEquipo(nuevoEquipo);
      orden.setUsuario(tecnico); // Tu DAO usa 'Usuario'

      // Campos iniciales para ReparacionDAO.insert
      orden.setDiagnosticoFinal(descripcionFalla);
      orden.setEstado("RECEPCIONADO"); // String "RECEPCIONADO", según tu DAO

      // Costos iniciales a cero (usando java.math.BigDecimal)
      orden.setCostoRepuestos(BigDecimal.ZERO);
      orden.setCostoManoObra(BigDecimal.ZERO);
      orden.setPresupuestoTotal(BigDecimal.ZERO);

      reparacionDAO.insert(orden); // Asumo que este insert es de tipo void

      // 4. Éxito y Redirección
      request.getSession().setAttribute("success", "Orden de Trabajo N° " + orden.getIdReparacion() + " creada exitosamente.");
      response.sendRedirect(request.getContextPath() + "/ReparacionController?action=listar");

    } catch (NumberFormatException e) {
      request.setAttribute("error", "Error de formato de ID. " + e.getMessage());
      request.getRequestDispatcher("/vistas/tecnico/formularioEquipo.jsp").forward(request, response);
    } catch (Exception e) {
      request.setAttribute("error", "Error al completar la orden: " + e.getMessage());
      request.getRequestDispatcher("/vistas/tecnico/formularioEquipo.jsp").forward(request, response);
    }
  }
}
