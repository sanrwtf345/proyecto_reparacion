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

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// 1. Activamos el motor de Mockito
@ExtendWith(MockitoExtension.class)
class ClienteServletTest {

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
  private ClienteServlet servlet;

  @Test
  void deberia_GuardarClienteCorrectamente_Cuando_DatosSonValidos() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("action")).thenReturn("guardar");

    // Simulamos los parámetros del formulario
    when(request.getParameter("nombre")).thenReturn("Lionel");
    when(request.getParameter("apellido")).thenReturn("Messi");
    when(request.getParameter("telefono")).thenReturn("123456789");
    when(request.getParameter("email")).thenReturn("leomessi@mail.com");

    // Simulamos la sesión y el usuario logueado
    when(request.getSession()).thenReturn(session);
    Usuario usuarioMock = new Usuario();
    when(session.getAttribute("usuarioLogueado")).thenReturn(usuarioMock);

    when(request.getContextPath()).thenReturn("/TallerApp");

    // --- 2. ACT ---
    servlet.doPost(request, response);

    // --- 3. ASSERT ---
    ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
    verify(clienteDAO).insert(captor.capture());

    Cliente clienteGuardado = captor.getValue();
    assertThat(clienteGuardado.getNombre()).isEqualTo("Lionel");
    assertThat(clienteGuardado.getApellido()).isEqualTo("Messi");
    assertThat(clienteGuardado.getUsuario()).isNotNull();

    verify(session).setAttribute(eq("success"), contains("registrado con éxito"));
    verify(response).sendRedirect("/TallerApp/ClienteController?action=listar");
  }

  @Test
  void deberia_MostrarError_Cuando_FaltaNombreOApellidoAlGuardar() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("action")).thenReturn("guardar");

    // Simulamos que el nombre viene vacío (falla la validación del Stream anyMatch)
    when(request.getParameter("nombre")).thenReturn("");
    when(request.getParameter("apellido")).thenReturn("Messi");

    when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

    // --- 2. ACT ---
    servlet.doPost(request, response);

    // --- 3. ASSERT ---
    verify(request).setAttribute(eq("error"), contains("El nombre y el apellido son obligatorios"));
    verify(clienteDAO, never()).insert(any(Cliente.class)); // Verificamos que se abortó la inserción
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_EliminarClienteYDependenciasEnCascada_Cuando_SeLlamaAEliminar() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("action")).thenReturn("eliminar");
    when(request.getParameter("idCliente")).thenReturn("10");

    // Preparamos la cascada: 1 Cliente -> 1 Equipo -> 2 Reparaciones
    Equipo equipoMock = new Equipo();
    equipoMock.setIdEquipo(5);
    List<Equipo> listaEquipos = java.util.Arrays.asList(equipoMock);

    Reparacion rep1 = new Reparacion(); rep1.setIdReparacion(101);
    Reparacion rep2 = new Reparacion(); rep2.setIdReparacion(102);
    List<Reparacion> listaReparaciones = java.util.Arrays.asList(rep1, rep2);

    // Configuramos los DAOs para que devuelvan nuestras listas falsas
    when(equipoDAO.getByClienteId(10)).thenReturn(listaEquipos);
    when(reparacionDAO.getByEquipoId(5)).thenReturn(listaReparaciones);

    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    // --- 2. ACT ---
    servlet.doGet(request, response);

    // --- 3. ASSERT ---
    // Verificamos que el forEach haya recorrido y eliminado todo en el orden correcto
    verify(reparacionDAO).delete(101);
    verify(reparacionDAO).delete(102);
    verify(equipoDAO).delete(5);
    verify(clienteDAO).delete(10); // El cliente se elimina al final

    verify(session).setAttribute(eq("success"), anyString());
    verify(response).sendRedirect("/TallerApp/ClienteController?action=listar");
  }

  @Test
  void deberia_ListarClientesFiltrados_Cuando_HayBusqueda() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("action")).thenReturn("listar");
    when(request.getParameter("busquedaApellido")).thenReturn("Messi");

    when(clienteDAO.getByApellido("Messi")).thenReturn(new ArrayList<>());
    when(request.getRequestDispatcher("/vistas/tecnico/listaClientes.jsp")).thenReturn(dispatcher);

    // --- 2. ACT ---
    servlet.doGet(request, response);

    // --- 3. ASSERT ---
    verify(clienteDAO).getByApellido("Messi");
    verify(clienteDAO, never()).getAll(); // Si buscó por apellido, no debió traer todos
    verify(request).setAttribute("busquedaActual", "Messi");
    verify(dispatcher).forward(request, response);
  }
}