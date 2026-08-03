package proyecto.interfaces.servlets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import proyecto.interfaces.dao.UsuarioDAO;
import proyecto.interfaces.entities.Usuario;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

// 1. Motor de Mockito: Le decimos a JUnit que usaremos Mocks en esta clase
@ExtendWith(MockitoExtension.class)
class UsuarioServletTest {

  // 2. Declaración de Mocks (Los "Impostores")
  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpSession session;

  @Mock
  private RequestDispatcher dispatcher;

  @Mock
  private UsuarioDAO usuarioDAO;

  // 3. InjectMocks: Crea el Servlet real y le inyecta los Mocks que declaramos arriba
  @InjectMocks
  private UsuarioServlet servlet;


  @Test
  void deberia_MostrarError_Cuando_FaltanDatosObligatoriosAlGuardar() throws Exception {
    // --- 1. ARRANGE (Preparar el escenario) ---

    // Fingimos que el usuario hizo submit en el formulario para "guardar"
    when(request.getParameter("action")).thenReturn("guardar");

    // Fingimos que dejó el correo vacío (nuestro caso borde)
    when(request.getParameter("correoElectronico")).thenReturn("");

    // Fingimos el despachador de vistas para que el Servlet no lance NullPointerException al redirigir
    when(request.getRequestDispatcher("/vistas/admin/formularioRegisUsuario.jsp")).thenReturn(dispatcher);

    // --- 2. ACT (Actuar) ---
    // Llamamos al método doPost del Servlet con nuestros objetos falsos
    servlet.doPost(request, response);

    // --- 3. ASSERT (Verificar comportamiento con Mockito) ---

    // A. Verificamos que el Servlet haya guardado un mensaje de "error" en el request
    verify(request).setAttribute(eq("error"), anyString());

    // B. Verificamos que nos haya devuelto al formulario original para corregir los datos
    verify(dispatcher).forward(request, response);

    // C. REGLA DE ORO: Verificamos que el DAO NUNCA haya intentado insertar en la base de datos
    verify(usuarioDAO, never()).insert(any(Usuario.class));
  }


  @Test
  void deberia_ListarUsuariosCorrectamente_Cuando_SeLlamaAlDoGetSinAccion() throws Exception {
    // --- 1. ARRANGE ---

    // Si la acción es null, el Servlet por defecto asume "listar"
    when(request.getParameter("action")).thenReturn(null);
    when(request.getParameter("busquedaApellido")).thenReturn(null);

    // Simulamos que la base de datos devuelve una lista vacía
    when(usuarioDAO.getAll()).thenReturn(new ArrayList<>());

    when(request.getRequestDispatcher("/vistas/admin/listadoUsuarios.jsp")).thenReturn(dispatcher);

    // --- 2. ACT ---
    servlet.doGet(request, response);

    // --- 3. ASSERT ---

    // Verificamos que consultó a la base de datos real a través del DAO
    verify(usuarioDAO).getAll();

    // Verificamos que mandó la "listaUsuarios" a la vista
    verify(request).setAttribute(eq("listaUsuarios"), anyList());

    // Verificamos que cargó el JSP del listado
    verify(dispatcher).forward(request, response);
  }
}