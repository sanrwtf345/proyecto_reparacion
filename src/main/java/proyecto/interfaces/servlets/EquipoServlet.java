package proyecto.interfaces.servlets;

import proyecto.interfaces.dao.ClienteDAO;
import proyecto.interfaces.dao.EquipoDAO;
import proyecto.interfaces.dao.ReparacionDAO;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Equipo;
import proyecto.interfaces.entities.Reparacion;
import proyecto.interfaces.entities.Usuarios; // Necesario para la sesión

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Servlet para manejar el registro de Equipo y Orden de Reparación
 * para un Cliente YA EXISTENTE en la base de datos.
 */
@WebServlet("/EquipoController")
public class EquipoServlet extends HttpServlet {
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

    if ("mostrarAgregarEquipo".equals(action)) {
      // Muestra el formulario para agregar equipo a cliente existente
      mostrarFormularioAgregarEquipo(request, response);
    } else if ("listarEquipos".equals(action)) {
      // Acción para listar (asumo que ReparcionController ya hace esto, pero lo mantenemos si es necesario)
      response.sendRedirect(request.getContextPath() + "/ReparacionController?action=listar");
    } else {
      // Acción por defecto (redirigir al menú o a la lista)
      response.sendRedirect(request.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");
    }
  }

  private void mostrarFormularioAgregarEquipo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      // Necesitamos pasar la lista de clientes al JSP para que el técnico pueda seleccionar uno
      List<Cliente> listaClientes = clienteDAO.getAll();
      request.setAttribute("listaClientes", listaClientes);
      request.getRequestDispatcher("/vistas/tecnico/agregarEquipo.jsp").forward(request, response);
    } catch (Exception e) {
      request.getSession().setAttribute("error", "Error al cargar la lista de clientes: " + e.getMessage());
      response.sendRedirect(request.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");

    if ("guardarNuevoEquipo".equals(action)) {
      guardarNuevoEquipo(request, response);
    } else {
      // Si la acción no se especifica, redirigir al menú
      response.sendRedirect(request.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");
    }
  }

  private void guardarNuevoEquipo(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // 1. OBTENER EL OBJETO USUARIO COMPLETO DE LA SESIÓN
    Usuarios tecnicoLogueado = (Usuarios) request.getSession().getAttribute("usuarioLogueado");
    Integer idUsuarioSesion = null;

    // Verificar si el objeto existe y extraer el ID
    if (tecnicoLogueado != null) {
      idUsuarioSesion = tecnicoLogueado.getIdUsuario();
    }

    // 2. VERIFICACIÓN DE SESIÓN (ESTE ES EL PUNTO CRÍTICO)
    if (idUsuarioSesion == null || idUsuarioSesion <= 0) {
      request.setAttribute("error", "Debe iniciar sesión como técnico para registrar un equipo.");

      // Si falla, debemos recargar los datos del formulario (lista de clientes)
      try {
        List<Cliente> listaClientes = clienteDAO.getAll();
        request.setAttribute("listaClientes", listaClientes);
      } catch (Exception daoE) {
        // Manejar error de carga de clientes si es crítico
        request.setAttribute("error", "Error crítico: No se pudo cargar la lista de clientes. " + daoE.getMessage());
      }

      request.getRequestDispatcher("/vistas/tecnico/agregarEquipo.jsp").forward(request, response);
      return;
    }

    // 3. Obtener parámetros y realizar validaciones
    String idClienteStr = request.getParameter("idCliente");
    String tipoEquipo = request.getParameter("tipoEquipo");
    String marca = request.getParameter("marca");
    String modelo = request.getParameter("modelo");
    String numSerie = request.getParameter("numSerie");
    String problemaReportado = request.getParameter("problemaReportado");

    if (idClienteStr == null || problemaReportado == null || problemaReportado.trim().isEmpty()) {
      request.setAttribute("error", "Datos incompletos: Asegúrese de seleccionar el cliente y describir la falla.");
      // Recargar datos y hacer forward en caso de error de validación
      mostrarFormularioAgregarEquipo(request, response);
      return;
    }

    try {
      int idCliente = Integer.parseInt(idClienteStr);

      // 4. Obtener Cliente (Necesario para la FK de Equipo) y Técnico (Necesario para Reparacion)
      Cliente cliente = clienteDAO.getById(idCliente);

      if (cliente == null) {
        throw new Exception("Cliente no encontrado en la base de datos.");
      }

      // Crear objeto técnico logueado (solo se necesita el ID)
      Usuarios tecnico = new Usuarios();
      tecnico.setIdUsuario(idUsuarioSesion);

      // 5. Registrar el Equipo
      Equipo nuevoEquipo = new Equipo();
      nuevoEquipo.setCliente(cliente);
      nuevoEquipo.setTipoEquipo(tipoEquipo);
      nuevoEquipo.setMarca(marca);
      nuevoEquipo.setModelo(modelo);
      nuevoEquipo.setNumeroSerie(numSerie);
      nuevoEquipo.setProblemaReportado(problemaReportado);

      equipoDAO.insert(nuevoEquipo);
      Integer idEquipo = nuevoEquipo.getIdEquipo();

      if (idEquipo == 0) {
        throw new Exception("Fallo al obtener el ID del Equipo.");
      }

      // 6. Registrar la Orden de Reparación
      Reparacion orden = new Reparacion();

      Equipo equipoPlaceholder = new Equipo();
      equipoPlaceholder.setIdEquipo(idEquipo); // Usamos el ID generado

      orden.setEquipo(equipoPlaceholder);
      orden.setUsuario(tecnico); // El técnico que registró la orden

      orden.setDiagnosticoFinal(problemaReportado);
      orden.setEstado("RECIBIDO");

      // Costos iniciales a cero
      orden.setCostoRepuestos(BigDecimal.ZERO);
      orden.setCostoManoObra(BigDecimal.ZERO);
      orden.setPresupuestoTotal(BigDecimal.ZERO);

      reparacionDAO.insert(orden);

      // 7. Éxito y Redirección
      request.getSession().setAttribute("success", "Equipo y Orden N° " + orden.getIdReparacion() + " creados con éxito.");
      response.sendRedirect(request.getContextPath() + "/ReparacionController?action=listar");

    } catch (NumberFormatException e) {
      request.setAttribute("error", "Error de formato de ID. " + e.getMessage());
      mostrarFormularioAgregarEquipo(request, response);
    } catch (Exception e) {
      request.setAttribute("error", "Error al completar la orden: " + e.getMessage());
      mostrarFormularioAgregarEquipo(request, response);
    }
  }
}
