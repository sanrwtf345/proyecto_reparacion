package proyecto.interfaces.servlets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyecto.interfaces.dao.ClienteDAO;
import proyecto.interfaces.dao.EquipoDAO;
import proyecto.interfaces.dao.ReparacionDAO;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Equipo;
import proyecto.interfaces.entities.Reparacion;
import proyecto.interfaces.entities.Usuario;
import proyecto.interfaces.enums.RolUsuario;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipoServletTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpSession session;

  @Mock
  private RequestDispatcher dispatcher;

  @Mock
  private ClienteDAO clienteDAO;

  @Mock
  private EquipoDAO equipoDAO;

  @Mock
  private ReparacionDAO reparacionDAO;

  @InjectMocks
  private EquipoServlet servlet;

  @Test
  void deberia_RedirigirAlMenu_Cuando_ActionEsNulo() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("action")).thenReturn(null);

    when(request.getSession()).thenReturn(session);
    Usuario tecnico = new Usuario();
    tecnico.setRol(RolUsuario.TECNICO);
    when(session.getAttribute("usuarioLogueado")).thenReturn(tecnico);
    when(request.getContextPath()).thenReturn("/TallerApp");

    // --- 2. ACT ---
    servlet.doGet(request, response);

    // --- 3. ASSERT ---
    // Verificamos que redirige al menú del técnico por defecto al no haber acción
    verify(response).sendRedirect("/TallerApp/vistas/tecnico/menuTecnico.jsp");
  }

  @Test
  void deberia_GuardarNuevoEquipo_Cuando_DatosSonValidos() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("action")).thenReturn("guardarNuevoEquipo");

    // Simulamos usuario en sesión
    when(request.getSession()).thenReturn(session);
    Usuario tecnico = new Usuario();
    tecnico.setIdUsuario(1);
    tecnico.setRol(RolUsuario.TECNICO);
    when(session.getAttribute("usuarioLogueado")).thenReturn(tecnico);

    // Simulamos parámetros válidos
    when(request.getParameter("idCliente")).thenReturn("5");
    when(request.getParameter("tipoEquipo")).thenReturn("Notebook");
    when(request.getParameter("problemaReportado")).thenReturn("Pantalla rota");
    when(request.getParameter("marca")).thenReturn("Lenovo");
    when(request.getParameter("modelo")).thenReturn("ThinkPad");
    when(request.getParameter("numSerie")).thenReturn("SN123");

    Cliente clienteMock = new Cliente();
    when(clienteDAO.getById(5)).thenReturn(clienteMock);
    when(request.getContextPath()).thenReturn("/TallerApp");

    // --- 2. ACT ---
    servlet.doPost(request, response);

    // --- 3. ASSERT ---
    ArgumentCaptor<Equipo> captor = ArgumentCaptor.forClass(Equipo.class);
    verify(equipoDAO).insert(captor.capture());

    Equipo equipoGuardado = captor.getValue();
    assertThat(equipoGuardado.getTipoEquipo()).isEqualTo("Notebook");
    assertThat(equipoGuardado.getProblemaReportado()).isEqualTo("Pantalla rota");

    verify(session).setAttribute(eq("success"), contains("registrado exitosamente"));
    verify(response).sendRedirect("/TallerApp/vistas/tecnico/menuTecnico.jsp");
  }

  @Test
  void deberia_MostrarError_Cuando_FaltanDatosObligatoriosAlActualizar() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("action")).thenReturn("actualizarEquipo");
    when(request.getParameter("idCliente")).thenReturn("5");

    // Simulamos que el tipo de equipo viene vacío para forzar la validación de Streams
    when(request.getParameter("tipoEquipo")).thenReturn("");
    when(request.getParameter("problemaReportado")).thenReturn("Falla de disco");

    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    // --- 2. ACT ---
    servlet.doPost(request, response);

    // --- 3. ASSERT ---
    // Verificamos que se captura la excepción de datos faltantes
    verify(session).setAttribute(eq("error"), contains("El tipo de equipo y el problema reportado son obligatorios"));

    // Verificamos que NUNCA intentó actualizar en la base de datos
    verify(equipoDAO, never()).update(any(Equipo.class));

    // Verificamos que redirigió de vuelta a la lista del cliente
    verify(response).sendRedirect("/TallerApp/EquipoController?action=listarPorCliente&idCliente=5");
  }

  @Test
  void deberia_EliminarEquipoYReparaciones_Cuando_SeEjecutaEliminacionEnCascada() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("action")).thenReturn("eliminarEquipo");
    when(request.getParameter("idEquipo")).thenReturn("15");
    when(request.getParameter("idCliente")).thenReturn("5");

    // Simulamos que el equipo tiene 2 reparaciones asociadas
    Reparacion rep1 = new Reparacion(); rep1.setIdReparacion(100);
    Reparacion rep2 = new Reparacion(); rep2.setIdReparacion(101);
    when(reparacionDAO.getByEquipoId(15)).thenReturn(java.util.Arrays.asList(rep1, rep2));

    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    // --- 2. ACT ---
    servlet.doGet(request, response);

    // --- 3. ASSERT ---
    // Comprobamos que el Consumer (forEach) eliminó ambas reparaciones primero
    verify(reparacionDAO).delete(100);
    verify(reparacionDAO).delete(101);

    // Comprobamos que luego se eliminó el equipo
    verify(equipoDAO).delete(15);

    verify(session).setAttribute(eq("success"), contains("eliminados"));
    verify(response).sendRedirect("/TallerApp/EquipoController?action=listarPorCliente&idCliente=5");
  }

  @Test
  void deberia_ListarEquiposPorCliente_Cuando_ClienteExiste() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("action")).thenReturn("listarPorCliente");
    when(request.getParameter("idCliente")).thenReturn("5");

    Cliente clienteMock = new Cliente();
    when(clienteDAO.getById(5)).thenReturn(clienteMock);
    when(equipoDAO.getByClienteId(5)).thenReturn(new ArrayList<>());

    when(request.getRequestDispatcher("/vistas/tecnico/listaEquiposPorCliente.jsp")).thenReturn(dispatcher);

    // --- 2. ACT ---
    servlet.doGet(request, response);

    // --- 3. ASSERT ---
    verify(request).setAttribute("cliente", clienteMock);
    verify(request).setAttribute(eq("listaEquipos"), anyList());
    verify(dispatcher).forward(request, response);
  }
}