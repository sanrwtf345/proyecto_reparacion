package proyecto.interfaces.servlets;

import proyecto.interfaces.dao.ReparacionDAO;
import proyecto.interfaces.entities.Reparacion;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Servlet que maneja la gestión de Órdenes de Reparación (CRUD, listados).
 */
@WebServlet("/ReparacionController")
public class ReparacionServlet extends HttpServlet {

  private ReparacionDAO reparacionDAO;

  @Override
  public void init() throws ServletException {
    // Inicializa el DAO
    this.reparacionDAO = new ReparacionDAO();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");

    if (action == null) {
      action = "listar"; // Acción por defecto
    }

    switch (action) {
      case "listar":
        listarReparaciones(request, response);
        break;
      // Otros casos futuros: "mostrarDetalle", "mostrarEditar", etc.
      default:
        listarReparaciones(request, response);
    }
  }

  private void listarReparaciones(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      // 1. Obtener la lista de órdenes desde el DAO
      List<Reparacion> listaReparaciones = reparacionDAO.getAll();

      // 2. Colocar la lista en el request para el JSP
      request.setAttribute("listaReparaciones", listaReparaciones);

      // 3. Forward al JSP de listado
      request.getRequestDispatcher("/vistas/tecnico/listaReparaciones.jsp").forward(request, response);

    } catch (Exception e) {
      // Manejo básico de errores
      request.setAttribute("error", "Error al cargar el listado de reparaciones: " + e.getMessage());
      request.getRequestDispatcher("/vistas/tecnico/menuTecnico.jsp").forward(request, response);
    }
  }

  // El doPost lo dejaremos vacío por ahora, ya que las acciones de estado/diagnóstico serán completadas en el futuro.
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }
}

