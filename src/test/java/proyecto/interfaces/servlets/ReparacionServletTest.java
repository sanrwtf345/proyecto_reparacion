package proyecto.interfaces.servlets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyecto.interfaces.dao.EquipoDAO;
import proyecto.interfaces.dao.ReparacionDAO;
import proyecto.interfaces.entities.Equipo;
import proyecto.interfaces.entities.Reparacion;
import proyecto.interfaces.entities.Usuario;
import proyecto.interfaces.enums.EstadoReparacion;
import proyecto.interfaces.enums.RolUsuario;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// 1. Motor de Mockito activado
@ExtendWith(MockitoExtension.class)
class ReparacionServletTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpSession session;

  @Mock
  private RequestDispatcher dispatcher;

  @Mock
  private ReparacionDAO reparacionDAO;

  @Mock
  private EquipoDAO equipoDAO;

  @InjectMocks
  private ReparacionServlet servlet;

  @Test
  void deberia_ListarTodasLasReparaciones_Cuando_NoHayFiltroEstado() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("action")).thenReturn("listar");
    when(request.getParameter("filtroEstado")).thenReturn(null); // Camino: sin filtro
    when(reparacionDAO.getAll()).thenReturn(new ArrayList<>());
    when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

    // --- 2. ACT ---
    servlet.doGet(request, response);

    // --- 3. ASSERT ---
    verify(reparacionDAO).getAll(); // Verificamos que llamó al método que trae todo
    verify(reparacionDAO, never()).getByEstado(any()); // Verificamos que NO llamó al filtro
    verify(request).setAttribute(eq("listaReparaciones"), anyList());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_ListarReparacionesFiltradas_Cuando_HayFiltroEstadoValido() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("action")).thenReturn("listar");
    when(request.getParameter("filtroEstado")).thenReturn("TERMINADO"); // Camino: con filtro
    when(reparacionDAO.getByEstado(EstadoReparacion.TERMINADO)).thenReturn(new ArrayList<>());
    when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

    // --- 2. ACT ---
    servlet.doGet(request, response);

    // --- 3. ASSERT ---
    verify(reparacionDAO).getByEstado(EstadoReparacion.TERMINADO); // Validamos que usó el método con filtro
    verify(request).setAttribute("estadoSeleccionado", "TERMINADO"); // Validó que guardó el filtro para el JSP
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_RedirigirAMenuAdmin_Cuando_FallaGuardarPorSesionExpirada() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("action")).thenReturn("guardar");
    when(request.getSession()).thenReturn(session);

    Usuario adminFake = new Usuario();
    adminFake.setRol(RolUsuario.ADMIN);

    // CORRECCIÓN: Le decimos a Mockito:
    // 1ra llamada (al verificar sesión) -> devuelve null (fuerza el error)
    // 2da llamada (dentro de manejarError) -> devuelve adminFake (para redirigir bien)
    when(session.getAttribute("usuarioLogueado")).thenReturn(null, adminFake);

    when(request.getContextPath()).thenReturn("/TallerApp");

    // --- 2. ACT ---
    servlet.doPost(request, response);

    // --- 3. ASSERT ---
    verify(session).setAttribute(eq("error"), contains("Sesión expirada"));
    verify(response).sendRedirect("/TallerApp/vistas/admin/menuAdmin.jsp");
    verify(reparacionDAO, never()).insert(any(Reparacion.class));
  }


  @Test
  void deberia_GuardarReparacionCorrectamente_CapturandoLosArgumentos() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("action")).thenReturn("guardar");
    when(request.getSession()).thenReturn(session);

    Usuario tecnico = new Usuario();
    tecnico.setRol(RolUsuario.TECNICO);
    when(session.getAttribute("usuarioLogueado")).thenReturn(tecnico);

    // Simulamos los parámetros del formulario
    when(request.getParameter("idEquipo")).thenReturn("5");
    // CORRECCIÓN: Usamos PENDIENTE que es un valor seguro del Enum
    when(request.getParameter("estado")).thenReturn("PENDIENTE");
    when(request.getParameter("diagnosticoFinal")).thenReturn("Cambio de pantalla");
    when(request.getParameter("costoRepuestos")).thenReturn("5000");
    when(request.getParameter("costoManoObra")).thenReturn("2000");
    when(request.getParameter("fechaDiagnostico")).thenReturn("2026-08-01");
    // Dejamos la fecha de entrega vacía para cubrir el Optional
    when(request.getParameter("fechaEntrega")).thenReturn("");

    when(request.getContextPath()).thenReturn("/TallerApp");

    // --- 2. ACT ---
    servlet.doPost(request, response);

    // --- 3. ASSERT ---
    ArgumentCaptor<Reparacion> captor = ArgumentCaptor.forClass(Reparacion.class);
    verify(reparacionDAO).insert(captor.capture());

    Reparacion reparacionGuardada = captor.getValue();

    assertThat(reparacionGuardada.getEstado()).isEqualTo(EstadoReparacion.PENDIENTE);
    assertThat(reparacionGuardada.getDiagnosticoFinal()).isEqualTo("Cambio de pantalla");
    assertThat(reparacionGuardada.getPresupuestoTotal()).isEqualByComparingTo("7000");

    verify(response).sendRedirect("/TallerApp/ReparacionController?action=listar");
  }
}