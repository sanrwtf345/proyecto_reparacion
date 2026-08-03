package proyecto.interfaces.servlets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import proyecto.interfaces.AdminConexion;
import proyecto.interfaces.dao.ClienteDAO;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Usuario;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Testcontainers
@ExtendWith(MockitoExtension.class)
class ClienteServletIntegrationTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("taller_db_test")
      .withUsername("test")
      .withPassword("test");

  private ClienteServlet servlet;
  private ClienteDAO clienteDAO;

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private HttpSession session;
  @Mock
  private RequestDispatcher dispatcher;

  @BeforeAll
  static void setupDatabase() throws Exception {
    System.setProperty("db.url", mysql.getJdbcUrl());
    System.setProperty("db.user", mysql.getUsername());
    System.setProperty("db.password", mysql.getPassword());

    AdminConexion.INSTANCE.recargarPoolParaTests();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("CREATE TABLE usuarios (id_usuario INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(50), apellido VARCHAR(50), correo_electronico VARCHAR(100), password VARCHAR(100), rol VARCHAR(20))");
      st.execute("CREATE TABLE clientes (id_cliente INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(50), apellido VARCHAR(50), telefono VARCHAR(20), email VARCHAR(50), id_usuario INT, FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario))");
      st.execute("CREATE TABLE equipo (id_equipo INT AUTO_INCREMENT PRIMARY KEY, id_cliente INT, tipo_equipo VARCHAR(50), marca VARCHAR(50), modelo VARCHAR(50), num_serie VARCHAR(50), problema_reportado VARCHAR(255), fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente))");
      st.execute("CREATE TABLE reparacion (id_reparacion INT AUTO_INCREMENT PRIMARY KEY, id_equipo INT, id_usuario INT, diagnostico_final VARCHAR(255), estado VARCHAR(50), costo_repuestos DECIMAL(10,2), costo_mano_obra DECIMAL(10,2), presupuesto_total DECIMAL(10,2), fecha_entrega_estimada DATE, fecha_diagnostico DATE, fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (id_equipo) REFERENCES equipo(id_equipo), FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario))");
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    servlet = new ClienteServlet();
    servlet.init();

    clienteDAO = new ClienteDAO();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("SET FOREIGN_KEY_CHECKS = 0");
      st.execute("TRUNCATE TABLE reparacion");
      st.execute("TRUNCATE TABLE equipo");
      st.execute("TRUNCATE TABLE clientes");
      st.execute("TRUNCATE TABLE usuarios");
      st.execute("SET FOREIGN_KEY_CHECKS = 1");

      st.execute("INSERT INTO usuarios (id_usuario, nombre) VALUES (1, 'TecnicoAdmin')");
      // Inyectamos un cliente de prueba por defecto
      st.execute("INSERT INTO clientes (id_cliente, nombre, apellido, telefono, email, id_usuario) VALUES (10, 'Lionel', 'Scaloni', '12345', 'leo@mail.com', 1)");
    }
  }

  // --- PRUEBAS ORIGINALES ---

  @Test
  void deberia_GuardarClienteEnBaseDeDatosReal() throws Exception {
    when(request.getParameter("action")).thenReturn("guardar");
    when(request.getParameter("nombre")).thenReturn("Emiliano");
    when(request.getParameter("apellido")).thenReturn("Martinez");
    when(request.getParameter("telefono")).thenReturn("555-DIBU");
    when(request.getParameter("email")).thenReturn("dibu@mail.com");

    when(request.getSession()).thenReturn(session);
    Usuario usuarioLogueado = new Usuario();
    usuarioLogueado.setIdUsuario(1);
    when(session.getAttribute("usuarioLogueado")).thenReturn(usuarioLogueado);

    when(request.getContextPath()).thenReturn("/TallerApp");

    servlet.doPost(request, response);

    verify(response).sendRedirect("/TallerApp/ClienteController?action=listar");
    List<Cliente> clientesEnBD = clienteDAO.getByApellido("Martinez");
    assertThat(clientesEnBD).hasSize(1);
  }

  @Test
  void deberia_EliminarClienteEnCascada() throws Exception {
    when(request.getParameter("action")).thenReturn("eliminar");
    when(request.getParameter("idCliente")).thenReturn("10");

    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    servlet.doGet(request, response);

    verify(response).sendRedirect("/TallerApp/ClienteController?action=listar");
    assertThat(clienteDAO.existsById(10)).isFalse();
  }

  // --- NUEVAS PRUEBAS PARA COBERTURA (Rutas secundarias y Validaciones) ---

  @Test
  void deberia_ListarClientes_SinFiltro() throws Exception {
    when(request.getParameter("action")).thenReturn("listar");
    when(request.getParameter("busquedaApellido")).thenReturn(null); // Sin búsqueda
    when(request.getRequestDispatcher("/vistas/tecnico/listaClientes.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute(eq("listaClientes"), anyList());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_ListarClientes_ConFiltroDeBusqueda() throws Exception {
    when(request.getParameter("action")).thenReturn("listar");
    when(request.getParameter("busquedaApellido")).thenReturn("Scaloni");
    when(request.getRequestDispatcher("/vistas/tecnico/listaClientes.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute(eq("busquedaActual"), eq("Scaloni"));
    verify(request).setAttribute(eq("listaClientes"), anyList());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_MostrarFormularioCrear() throws Exception {
    when(request.getParameter("action")).thenReturn("crear");
    when(request.getRequestDispatcher("/vistas/tecnico/formularioCliente.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    // Debe mandar un cliente vacío al JSP
    verify(request).setAttribute(eq("cliente"), any(Cliente.class));
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_MostrarFormularioEditar() throws Exception {
    when(request.getParameter("action")).thenReturn("editar");
    when(request.getParameter("idCliente")).thenReturn("10"); // Cliente insertado en @BeforeEach
    when(request.getRequestDispatcher("/vistas/tecnico/formularioCliente.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    // Comprueba que extrajo correctamente el nombre del cliente de la BD
    verify(request).setAttribute(eq("cliente"), argThat(c -> ((Cliente) c).getNombre().equals("Lionel")));
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_RechazarGuardado_CuandoFaltanDatosObligatorios() throws Exception {
    when(request.getParameter("action")).thenReturn("guardar");
    when(request.getParameter("nombre")).thenReturn(""); // Vacío intencionalmente
    when(request.getParameter("apellido")).thenReturn("");

    when(request.getRequestDispatcher("/vistas/tecnico/formularioCliente.jsp")).thenReturn(dispatcher);

    servlet.doPost(request, response);

    // Verifica que la excepción se capturó y se devolvió como atributo "error"
    verify(request).setAttribute(eq("error"), contains("El nombre y el apellido son obligatorios"));
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_ActualizarCliente_EnLaBaseDeDatos() throws Exception {
    when(request.getParameter("action")).thenReturn("actualizar");
    when(request.getParameter("idCliente")).thenReturn("10");
    when(request.getParameter("nombre")).thenReturn("Lionel Andres"); // Nombre modificado
    when(request.getParameter("apellido")).thenReturn("Scaloni");
    when(request.getParameter("telefono")).thenReturn("99999");
    when(request.getParameter("email")).thenReturn("dt@mail.com");

    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    servlet.doPost(request, response);

    verify(response).sendRedirect("/TallerApp/ClienteController?action=listar");

    Cliente actualizado = clienteDAO.getById(10);
    assertThat(actualizado.getNombre()).isEqualTo("Lionel Andres");
    assertThat(actualizado.getTelefono()).isEqualTo("99999");
  }

  @Test
  void deberia_ManejarErrorInterno_Cuando_IdClienteEsInvalido() throws Exception {
    // Arrange
    when(request.getParameter("action")).thenReturn("editar");
    when(request.getParameter("idCliente")).thenReturn("invalido"); // Falla el parseo a Integer

    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    // Act
    servlet.doGet(request, response);

    // Assert
    verify(session).setAttribute(eq("error"), anyString());
    verify(response).sendRedirect("/TallerApp/ClienteController?action=listar");
  }

}