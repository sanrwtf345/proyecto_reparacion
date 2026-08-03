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
import proyecto.interfaces.dao.EquipoDAO;
import proyecto.interfaces.entities.Equipo;
import proyecto.interfaces.entities.Usuario;
import proyecto.interfaces.enums.RolUsuario;

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
class EquipoServletIntegrationTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("taller_db_test")
      .withUsername("test")
      .withPassword("test");

  private EquipoServlet servlet;
  private EquipoDAO equipoDAO;

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
    servlet = new EquipoServlet();
    servlet.init();

    equipoDAO = new EquipoDAO();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("SET FOREIGN_KEY_CHECKS = 0");
      st.execute("TRUNCATE TABLE reparacion");
      st.execute("TRUNCATE TABLE equipo");
      st.execute("TRUNCATE TABLE clientes");
      st.execute("TRUNCATE TABLE usuarios");
      st.execute("SET FOREIGN_KEY_CHECKS = 1");

      // Datos semilla
      st.execute("INSERT INTO usuarios (id_usuario, nombre) VALUES (1, 'Tecnico1')");
      st.execute("INSERT INTO clientes (id_cliente, nombre, apellido, id_usuario) VALUES (1, 'Carlos', 'Perez', 1)");
      // Equipo semilla para pruebas de lectura/actualización
      st.execute("INSERT INTO equipo (id_equipo, id_cliente, tipo_equipo, problema_reportado) VALUES (20, 1, 'Tablet', 'Pantalla rota')");
    }
  }

  // --- PRUEBAS ORIGINALES ---

  @Test
  void deberia_GuardarNuevoEquipoEnBaseDeDatosReal_DesdeElServlet() throws Exception {
    when(request.getParameter("action")).thenReturn("guardarNuevoEquipo");
    when(request.getParameter("idCliente")).thenReturn("1");
    when(request.getParameter("tipoEquipo")).thenReturn("Impresora");
    when(request.getParameter("marca")).thenReturn("Epson");
    when(request.getParameter("modelo")).thenReturn("L3150");
    when(request.getParameter("numSerie")).thenReturn("SN-999");
    when(request.getParameter("problemaReportado")).thenReturn("Atasco de papel");

    when(request.getSession()).thenReturn(session);
    Usuario tecnico = new Usuario();
    tecnico.setIdUsuario(1);
    tecnico.setRol(RolUsuario.TECNICO);
    when(session.getAttribute("usuarioLogueado")).thenReturn(tecnico);

    when(request.getContextPath()).thenReturn("/TallerApp");

    servlet.doPost(request, response);

    verify(response).sendRedirect("/TallerApp/vistas/tecnico/menuTecnico.jsp");

    List<Equipo> equiposDelCliente = equipoDAO.getByClienteId(1);
    assertThat(equiposDelCliente).hasSize(2); // La Tablet semilla + la Impresora nueva

    // Verificamos que la impresora se guardó bien
    boolean existeImpresora = equiposDelCliente.stream().anyMatch(e -> e.getTipoEquipo().equals("Impresora"));
    assertThat(existeImpresora).isTrue();
  }

  @Test
  void deberia_EliminarEquipoYSusReparacionesEnBDReal_DesdeElServlet() throws Exception {
    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {
      st.execute("INSERT INTO reparacion (id_reparacion, id_equipo, id_usuario, estado) VALUES (101, 20, 1, 'PENDIENTE')");
    }

    when(request.getParameter("action")).thenReturn("eliminarEquipo");
    when(request.getParameter("idEquipo")).thenReturn("20");
    when(request.getParameter("idCliente")).thenReturn("1");

    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    servlet.doGet(request, response);

    verify(response).sendRedirect("/TallerApp/EquipoController?action=listarPorCliente&idCliente=1");

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      java.sql.ResultSet rsEquipo = st.executeQuery("SELECT count(*) FROM equipo WHERE id_equipo = 20");
      rsEquipo.next();
      assertThat(rsEquipo.getInt(1)).isEqualTo(0);

      java.sql.ResultSet rsReparaciones = st.executeQuery("SELECT count(*) FROM reparacion WHERE id_equipo = 20");
      rsReparaciones.next();
      assertThat(rsReparaciones.getInt(1)).isEqualTo(0);
    }
  }

  // --- NUEVAS PRUEBAS PARA COBERTURA ---

  @Test
  void deberia_ListarEquiposPorCliente() throws Exception {
    when(request.getParameter("action")).thenReturn("listarPorCliente");
    when(request.getParameter("idCliente")).thenReturn("1");
    when(request.getRequestDispatcher("/vistas/tecnico/listaEquiposPorCliente.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute(eq("listaEquipos"), anyList());
    verify(request).setAttribute(eq("cliente"), any());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_MostrarFormularioAgregarEquipo() throws Exception {
    when(request.getParameter("action")).thenReturn("mostrarAgregarEquipo");
    when(request.getRequestDispatcher("/vistas/tecnico/agregarEquipo.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute(eq("listaClientes"), anyList());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_MostrarFormularioEditarEquipo() throws Exception {
    when(request.getParameter("action")).thenReturn("mostrarEditarEquipo");
    when(request.getParameter("idEquipo")).thenReturn("20");
    when(request.getRequestDispatcher("/vistas/tecnico/formularioEditarEquipo.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute(eq("equipo"), any(Equipo.class));
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_VerHistorialDeEquipo() throws Exception {
    when(request.getParameter("action")).thenReturn("verHistorial");
    when(request.getParameter("idEquipo")).thenReturn("20");
    when(request.getRequestDispatcher("/vistas/tecnico/historialReparaciones.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute(eq("equipo"), any(Equipo.class));
    verify(request).setAttribute(eq("historial"), anyList());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_RechazarGuardado_CuandoFaltanDatosObligatorios() throws Exception {
    when(request.getParameter("action")).thenReturn("guardarNuevoEquipo");
    when(request.getParameter("idCliente")).thenReturn("1");
    // Dejamos tipoEquipo y problemaReportado vacíos
    when(request.getParameter("tipoEquipo")).thenReturn("");
    when(request.getParameter("problemaReportado")).thenReturn("");

    when(request.getSession()).thenReturn(session);
    Usuario tecnico = new Usuario();
    tecnico.setIdUsuario(1);
    when(session.getAttribute("usuarioLogueado")).thenReturn(tecnico);

    when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

    servlet.doPost(request, response);

    // Como falla, el catch ejecuta mostrarFormularioAgregarEquipo que usa el request dispatcher
    verify(request).setAttribute(eq("error"), contains("obligatorios"));
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_ActualizarEquipo_EnBaseDeDatosReal() throws Exception {
    when(request.getParameter("action")).thenReturn("actualizarEquipo");
    when(request.getParameter("idEquipo")).thenReturn("20");
    when(request.getParameter("idCliente")).thenReturn("1");

    when(request.getParameter("tipoEquipo")).thenReturn("Celular"); // Cambiamos de Tablet a Celular
    when(request.getParameter("marca")).thenReturn("Samsung");
    when(request.getParameter("modelo")).thenReturn("S23");
    when(request.getParameter("numSerie")).thenReturn("12345");
    when(request.getParameter("problemaReportado")).thenReturn("Bateria agotada"); // Cambiamos el problema

    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    servlet.doPost(request, response);

    verify(response).sendRedirect("/TallerApp/EquipoController?action=listarPorCliente&idCliente=1");

    Equipo equipoActualizado = equipoDAO.getById(20);
    assertThat(equipoActualizado.getTipoEquipo()).isEqualTo("Celular");
    assertThat(equipoActualizado.getProblemaReportado()).isEqualTo("Bateria agotada");
  }

  @Test
  void deberia_ManejarError_Cuando_IdEquipoEsInvalido() throws Exception {
    // --- 1. ARRANGE ---
    when(request.getParameter("action")).thenReturn("eliminarEquipo");
    when(request.getParameter("idEquipo")).thenReturn("letras"); // Provoca el fallo inmediato

    // Usamos lenient() para decirle a Mockito que "no se enoje" si el bloque catch
    // decide no usar el contextPath o la sesión en este flujo específico.
    lenient().when(request.getSession()).thenReturn(session);
    lenient().when(request.getContextPath()).thenReturn("/TallerApp");

    // Nota: Eliminamos la simulación del Usuario porque el sistema explota
    // antes de siquiera preguntar quién está logueado.

    // --- 2. ACT ---
    servlet.doGet(request, response);

    // --- 3. ASSERT ---
    // Verificamos que el error fue atrapado y se guardó en la sesión
    verify(session).setAttribute(eq("error"), anyString());
  }

}