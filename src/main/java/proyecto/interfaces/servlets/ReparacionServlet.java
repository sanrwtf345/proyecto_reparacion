package proyecto.interfaces.servlets;

import proyecto.interfaces.dao.EquipoDAO;
import proyecto.interfaces.dao.ReparacionDAO;
import proyecto.interfaces.entities.Equipo;
import proyecto.interfaces.entities.Reparacion;
import proyecto.interfaces.entities.Usuario;
import proyecto.interfaces.enums.EstadoReparacion;
import proyecto.interfaces.enums.RolUsuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@WebServlet("/ReparacionController")
public class ReparacionServlet extends HttpServlet {

  private ReparacionDAO reparacionDAO;
  private EquipoDAO equipoDAO;

  @Override
  public void init() throws ServletException {
    this.reparacionDAO = new ReparacionDAO();
    this.equipoDAO = new EquipoDAO();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");

    try {
      if (action == null) action = "listar";

      switch (action) {
        case "listar":
          listarReparaciones(request, response);
          break;
        case "nueva":
          mostrarFormularioNueva(request, response);
          break;
        case "editar":
          mostrarFormularioEdicion(request, response);
          break;
        case "verDetalle": // Reutilizamos el formulario de edición en modo lectura o normal
          mostrarFormularioEdicion(request, response);
          break;
        case "eliminar":
          eliminarReparacion(request, response);
          break;
        default:
          listarReparaciones(request, response);
      }
    } catch (Exception e) {
      manejarError(request, response, e);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");

    try {
      if ("guardar".equals(action)) {
        guardarReparacion(request, response);
      } else if ("actualizar".equals(action)) {
        actualizarReparacion(request, response);
      } else {
        listarReparaciones(request, response);
      }
    } catch (Exception e) {
      manejarError(request, response, e);
    }
  }

  // --- MÉTODOS DE VISUALIZACIÓN ---

  private void listarReparaciones(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    List<Reparacion> lista = reparacionDAO.getAll();
    request.setAttribute("listaReparaciones", lista);
    request.getRequestDispatcher("/vistas/tecnico/listaReparaciones.jsp").forward(request, response);
  }

  private void mostrarFormularioNueva(HttpServletRequest request, HttpServletResponse response) throws Exception {
    // Para crear una orden, NECESITAMOS el ID del equipo
    int idEquipo = Integer.parseInt(request.getParameter("idEquipo"));
    Equipo equipo = equipoDAO.getById(idEquipo);

    if (equipo == null) throw new Exception("Equipo no encontrado.");

    Reparacion reparacion = new Reparacion();
    reparacion.setEquipo(equipo);

    // Valores por defecto
    reparacion.setEstado(EstadoReparacion.PENDIENTE);
    reparacion.setDiagnosticoFinal(equipo.getProblemaReportado()); // Copiamos la falla inicial como diagnóstico base

    prepararDatosFormulario(request, reparacion);
    request.setAttribute("titulo", "Nueva Orden de Reparación");
    request.getRequestDispatcher("/vistas/tecnico/formularioReparacion.jsp").forward(request, response);
  }

  private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response) throws Exception {
    int idReparacion = Integer.parseInt(request.getParameter("id"));
    Reparacion reparacion = reparacionDAO.getById(idReparacion);

    if (reparacion == null) throw new Exception("Reparación no encontrada.");

    prepararDatosFormulario(request, reparacion);
    request.setAttribute("titulo", "Gestionar Orden N° " + idReparacion);
    request.getRequestDispatcher("/vistas/tecnico/formularioReparacion.jsp").forward(request, response);
  }

  // Helper para enviar datos comunes al JSP
  private void prepararDatosFormulario(HttpServletRequest request, Reparacion reparacion) {
    request.setAttribute("reparacion", reparacion);
    // Enviamos los valores del Enum para llenar el <select>
    request.setAttribute("listaEstados", Arrays.asList(EstadoReparacion.values()));
  }

  // --- MÉTODOS DE ACCIÓN (GUARDAR/ACTUALIZAR) ---

  private void guardarReparacion(HttpServletRequest request, HttpServletResponse response) throws Exception {
    Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("usuarioLogueado");
    if (usuarioLogueado == null) throw new Exception("Sesión expirada.");

    Reparacion r = mapearFormulario(request);
    r.setUsuario(usuarioLogueado); // Asignar al técnico que crea la orden

    reparacionDAO.insert(r);

    request.getSession().setAttribute("success", "Orden de reparación creada correctamente.");
    response.sendRedirect(request.getContextPath() + "/ReparacionController?action=listar");
  }

  private void actualizarReparacion(HttpServletRequest request, HttpServletResponse response) throws Exception {
    Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("usuarioLogueado");
    if (usuarioLogueado == null) throw new Exception("Sesión expirada.");

    Reparacion r = mapearFormulario(request);
    r.setIdReparacion(Integer.parseInt(request.getParameter("idReparacion")));
    r.setUsuario(usuarioLogueado); // Actualizamos al técnico que modificó (opcional)

    reparacionDAO.update(r);

    request.getSession().setAttribute("success", "Orden actualizada correctamente.");
    response.sendRedirect(request.getContextPath() + "/ReparacionController?action=listar");
  }

  private void eliminarReparacion(HttpServletRequest request, HttpServletResponse response) throws Exception {
    int id = Integer.parseInt(request.getParameter("id"));
    reparacionDAO.delete(id);
    request.getSession().setAttribute("success", "Orden eliminada.");
    response.sendRedirect(request.getContextPath() + "/ReparacionController?action=listar");
  }

  // Helper para leer los datos del POST
  private Reparacion mapearFormulario(HttpServletRequest request) {
    Reparacion r = new Reparacion();

    // ID Equipo
    Equipo e = new Equipo();
    e.setIdEquipo(Integer.parseInt(request.getParameter("idEquipo")));
    r.setEquipo(e);

    // Datos
    r.setDiagnosticoFinal(request.getParameter("diagnosticoFinal"));
    r.setEstado(EstadoReparacion.valueOf(request.getParameter("estado")));

    // Costos (Manejo de nulos/vacíos)
    r.setCostoRepuestos(parseBigDecimal(request.getParameter("costoRepuestos")));
    r.setCostoManoObra(parseBigDecimal(request.getParameter("costoManoObra")));
    r.calcularTotal(); // Método de la entidad que suma

    // Fechas
    String fechaDiag = request.getParameter("fechaDiagnostico");
    if (fechaDiag != null && !fechaDiag.isEmpty()) r.setFechaDiagnostico(LocalDate.parse(fechaDiag));

    String fechaEnt = request.getParameter("fechaEntrega");
    if (fechaEnt != null && !fechaEnt.isEmpty()) r.setFechaEntregaEstimada(LocalDate.parse(fechaEnt));

    return r;
  }

  private BigDecimal parseBigDecimal(String valor) {
    if (valor == null || valor.trim().isEmpty()) return BigDecimal.ZERO;
    return new BigDecimal(valor);
  }

  private void manejarError(HttpServletRequest request, HttpServletResponse response, Exception e) throws ServletException, IOException {
    request.getSession().setAttribute("error", "Error: " + e.getMessage());
    redirigirAlMenu(request, response);
  }

  private void redirigirAlMenu(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Usuario u = (Usuario) request.getSession().getAttribute("usuarioLogueado");
    if (u != null && u.getRol() == RolUsuario.ADMIN) {
      response.sendRedirect(request.getContextPath() + "/vistas/admin/menuAdmin.jsp");
    } else {
      response.sendRedirect(request.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");
    }
  }
}

