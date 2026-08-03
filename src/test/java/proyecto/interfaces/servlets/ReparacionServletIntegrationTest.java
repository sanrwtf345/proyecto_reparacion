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
import proyecto.interfaces.dao.ReparacionDAO;
import proyecto.interfaces.entities.Reparacion;
import proyecto.interfaces.entities.Usuario;
import proyecto.interfaces.enums.EstadoReparacion;
import proyecto.interfaces.enums.RolUsuario;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Testcontainers
@ExtendWith(MockitoExtension.class)
class ReparacionServletIntegrationTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("taller_db_test")
      .withUsername("test")
      .withPassword("test");

  private ReparacionServlet servlet;
  private ReparacionDAO reparacionDAO;

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
    servlet = new ReparacionServlet();
    servlet.init();

    reparacionDAO = new ReparacionDAO();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("SET FOREIGN_KEY_CHECKS = 0");
      st.execute("TRUNCATE TABLE reparacion");
      st.execute("TRUNCATE TABLE equipo");
      st.execute("TRUNCATE TABLE clientes");
      st.execute("TRUNCATE TABLE usuarios");
      st.execute("SET FOREIGN_KEY_CHECKS = 1");

      // Datos semilla
      st.execute("INSERT INTO usuarios (id_usuario, nombre) VALUES (1, 'TecnicoAdmin')");
      st.execute("INSERT INTO clientes (id_cliente, nombre, apellido, id_usuario) VALUES (1, 'Juan', 'Perez', 1)");
      st.execute("INSERT INTO equipo (id_equipo, id_cliente, tipo_equipo, problema_reportado) VALUES (1, 1, 'Notebook', 'No enciende')");

      // Reparación semilla (ID 50) para pruebas de lectura/actualización
      st.execute("INSERT INTO reparacion (id_reparacion, id_equipo, id_usuario, diagnostico_final, estado, costo_repuestos, costo_mano_obra, presupuesto_total) VALUES (50, 1, 1, 'Mantenimiento preventivo', 'PENDIENTE', 0.00, 5000.00, 5000.00)");
    }
  }

  // --- PRUEBAS ORIGINALES ---

  @Test
  void deberia_GuardarNuevaReparacionEnBaseDeDatosReal_CalculandoCostos() throws Exception {
    when(request.getParameter("action")).thenReturn("guardar");
    when(request.getParameter("idEquipo")).thenReturn("1");
    when(request.getParameter("diagnosticoFinal")).thenReturn("Cambio de placa madre");
    when(request.getParameter("estado")).thenReturn("EN_PROCESO");
    when(request.getParameter("costoRepuestos")).thenReturn("25000.50");
    when(request.getParameter("costoManoObra")).thenReturn("10000.00");
    when(request.getParameter("fechaDiagnostico")).thenReturn("");
    when(request.getParameter("fechaEntrega")).thenReturn("");

    when(request.getSession()).thenReturn(session);
    Usuario tecnico = new Usuario();
    tecnico.setIdUsuario(1);
    tecnico.setRol(RolUsuario.TECNICO);
    when(session.getAttribute("usuarioLogueado")).thenReturn(tecnico);

    when(request.getContextPath()).thenReturn("/TallerApp");

    servlet.doPost(request, response);

    verify(response).sendRedirect("/TallerApp/ReparacionController?action=listar");

    List<Reparacion> todasLasReparaciones = reparacionDAO.getAll();
    assertThat(todasLasReparaciones).hasSize(2); // La semilla (ID 50) + la nueva

    Reparacion guardada = todasLasReparaciones.stream().filter(r -> r.getIdReparacion() != 50).findFirst().get();
    assertThat(guardada.getDiagnosticoFinal()).isEqualTo("Cambio de placa madre");
    assertThat(guardada.getEstado()).isEqualTo(EstadoReparacion.EN_PROCESO);
    assertThat(guardada.getPresupuestoTotal()).isEqualByComparingTo(new BigDecimal("35000.50"));
  }

  @Test
  void deberia_EliminarReparacionEnBDReal_DesdeElServlet() throws Exception {
    when(request.getParameter("action")).thenReturn("eliminar");
    when(request.getParameter("id")).thenReturn("50");

    when(request.getSession()).thenReturn(session);
    when(request.getContextPath()).thenReturn("/TallerApp");

    servlet.doGet(request, response);

    verify(response).sendRedirect("/TallerApp/ReparacionController?action=listar");

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {
      java.sql.ResultSet rs = st.executeQuery("SELECT count(*) FROM reparacion WHERE id_reparacion = 50");
      rs.next();
      assertThat(rs.getInt(1)).isEqualTo(0);
    }
  }

  // --- NUEVAS PRUEBAS PARA COBERTURA ---

  @Test
  void deberia_ListarTodasLasReparaciones_SinFiltro() throws Exception {
    when(request.getParameter("action")).thenReturn("listar");
    when(request.getParameter("filtroEstado")).thenReturn(null);
    when(request.getRequestDispatcher("/vistas/tecnico/listaReparaciones.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute(eq("listaReparaciones"), anyList());
    verify(request).setAttribute(eq("listaEstados"), anyList());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_ListarReparaciones_ConFiltroDeEstado() throws Exception {
    when(request.getParameter("action")).thenReturn("listar");
    when(request.getParameter("filtroEstado")).thenReturn("PENDIENTE"); // Filtro válido
    when(request.getRequestDispatcher("/vistas/tecnico/listaReparaciones.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute(eq("estadoSeleccionado"), eq("PENDIENTE"));
    verify(request).setAttribute(eq("listaReparaciones"), anyList());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_MostrarFormularioNuevaReparacion() throws Exception {
    when(request.getParameter("action")).thenReturn("nueva");
    when(request.getParameter("idEquipo")).thenReturn("1"); // Equipo existente
    when(request.getRequestDispatcher("/vistas/tecnico/formularioReparacion.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute(eq("titulo"), contains("Nueva Orden de Reparación"));
    verify(request).setAttribute(eq("reparacion"), any(Reparacion.class));
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_MostrarFormularioEdicionReparacion() throws Exception {
    when(request.getParameter("action")).thenReturn("editar");
    when(request.getParameter("id")).thenReturn("50"); // Reparación existente
    when(request.getRequestDispatcher("/vistas/tecnico/formularioReparacion.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute(eq("titulo"), contains("Gestionar Orden N° 50"));
    verify(request).setAttribute(eq("reparacion"), any(Reparacion.class));
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_ActualizarReparacion_RecalculandoCostos() throws Exception {
    when(request.getParameter("action")).thenReturn("actualizar");
    when(request.getParameter("idReparacion")).thenReturn("50"); // Editamos la semilla
    when(request.getParameter("idEquipo")).thenReturn("1");
    when(request.getParameter("diagnosticoFinal")).thenReturn("Limpieza profunda");
    when(request.getParameter("estado")).thenReturn("FINALIZADO");
    when(request.getParameter("costoRepuestos")).thenReturn("2000.00"); // Nuevos valores
    when(request.getParameter("costoManoObra")).thenReturn("8000.00");
    when(request.getParameter("fechaDiagnostico")).thenReturn("2026-08-01"); // Usando Optional Date
    when(request.getParameter("fechaEntrega")).thenReturn("");

    when(request.getSession()).thenReturn(session);
    Usuario tecnico = new Usuario();
    tecnico.setIdUsuario(1);
    when(session.getAttribute("usuarioLogueado")).thenReturn(tecnico);

    when(request.getContextPath()).thenReturn("/TallerApp");

    servlet.doPost(request, response);

    verify(response).sendRedirect("/TallerApp/ReparacionController?action=listar");

    Reparacion reparacionActualizada = reparacionDAO.getById(50);
    assertThat(reparacionActualizada.getEstado()).isEqualTo(EstadoReparacion.FINALIZADO);
    assertThat(reparacionActualizada.getPresupuestoTotal()).isEqualByComparingTo(new BigDecimal("10000.00")); // 2k + 8k
  }

  @Test
  void deberia_ManejarError_CuandoFallaBusquedaDeEquipoParaFormulario() throws Exception {
    when(request.getParameter("action")).thenReturn("nueva");
    when(request.getParameter("idEquipo")).thenReturn("999"); // ID Inexistente

    when(request.getSession()).thenReturn(session);
    Usuario admin = new Usuario();
    admin.setRol(RolUsuario.ADMIN);
    when(session.getAttribute("usuarioLogueado")).thenReturn(admin);
    when(request.getContextPath()).thenReturn("/TallerApp");

    servlet.doGet(request, response);

    // Verifica que la excepción se capturó y nos devolvió al menú
    verify(session).setAttribute(eq("error"), contains("Equipo no encontrado"));
    verify(response).sendRedirect("/TallerApp/vistas/admin/menuAdmin.jsp");
  }
}