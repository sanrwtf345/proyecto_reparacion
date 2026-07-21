package proyecto.interfaces.dao;

import proyecto.interfaces.AdminConexion;
import proyecto.interfaces.DAO;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Equipo;
import proyecto.interfaces.entities.Reparacion;
import proyecto.interfaces.entities.Usuario;
import proyecto.interfaces.enums.EstadoReparacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReparacionDAO implements DAO<Reparacion, Integer> {

  // Defino todas las consultas SQL como constantes para tenerlas centralizadas y ordenadas.

  // SQL: Insertar todos los campos (incluyendo fechas y costos)
  private static final String SQL_INSERT =
      "INSERT INTO reparacion (id_equipo, id_usuario, diagnostico_final, estado, " +
          "costo_repuestos, costo_mano_obra, presupuesto_total, fecha_entrega_estimada, fecha_diagnostico) " +
          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String SQL_UPDATE =
      "UPDATE reparacion SET id_usuario = ?, diagnostico_final = ?, estado = ?, " +
          "costo_repuestos = ?, costo_mano_obra = ?, presupuesto_total = ?, " +
          "fecha_entrega_estimada = ?, fecha_diagnostico = ? " +
          "WHERE id_reparacion = ?";

  // SQL: Obtener lista completa con JOINs para mostrar nombres de cliente, equipo y técnico.
  private static final String SQL_GETALL =
      "SELECT r.*, e.tipo_equipo, e.marca, e.modelo, c.nombre AS nombre_cliente, c.apellido AS apellido_cliente, " +
          "u.nombre AS nombre_usuario " +
          "FROM reparacion r " +
          "JOIN equipo e ON r.id_equipo = e.id_equipo " +
          "JOIN clientes c ON e.id_cliente = c.id_cliente " +
          "JOIN usuarios u ON r.id_usuario = u.id_usuario " +
          "ORDER BY r.fecha_creacion DESC";

  // SQL: Obtener detalle completo por ID
  private static final String SQL_GETBYID =
      "SELECT r.*, e.id_equipo, e.tipo_equipo, e.marca, e.modelo, e.num_serie, e.problema_reportado, " +
          "c.id_cliente, c.nombre AS nombre_cliente, c.apellido AS apellido_cliente, c.telefono, c.email, " +
          "u.id_usuario, u.nombre AS nombre_usuario, u.apellido AS apellido_usuario " +
          "FROM reparacion r " +
          "JOIN equipo e ON r.id_equipo = e.id_equipo " +
          "JOIN clientes c ON e.id_cliente = c.id_cliente " +
          "JOIN usuarios u ON r.id_usuario = u.id_usuario " +
          "WHERE r.id_reparacion = ?";

  private static final String SQL_DELETE = "DELETE FROM reparacion WHERE id_reparacion = ?";
  private static final String SQL_GETBY_EQUIPO = "SELECT * FROM reparacion WHERE id_equipo = ?";
  private static final String SQL_EXISTS = "SELECT id_reparacion FROM reparacion WHERE id_reparacion = ?";

  private static final String SQL_GET_BY_ESTADO =
      "SELECT r.*, e.tipo_equipo, e.marca, e.modelo, c.nombre AS nombre_cliente, c.apellido AS apellido_cliente, " +
          "u.nombre AS nombre_usuario " +
          "FROM reparacion r " +
          "JOIN equipo e ON r.id_equipo = e.id_equipo " +
          "JOIN clientes c ON e.id_cliente = c.id_cliente " +
          "JOIN usuarios u ON r.id_usuario = u.id_usuario " +
          "WHERE r.estado = ? " +
          "ORDER BY r.fecha_creacion DESC";

  private static final String SQL_GET_HISTORIAL_POR_EQUIPO =
      "SELECT r.*, e.tipo_equipo, e.marca, e.modelo, c.nombre AS nombre_cliente, c.apellido AS apellido_cliente, " +
          "u.nombre AS nombre_usuario " +
          "FROM reparacion r " +
          "JOIN equipo e ON r.id_equipo = e.id_equipo " +
          "JOIN clientes c ON e.id_cliente = c.id_cliente " +
          "JOIN usuarios u ON r.id_usuario = u.id_usuario " +
          "WHERE r.id_equipo = ? " +
          "ORDER BY r.fecha_creacion DESC";

  @Override
  public List<Reparacion> getAll() {
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Reparacion> lista = new ArrayList<>();

    try {
      // Ejecuto la consulta general para traer todas las reparaciones.
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_GETALL);
      rs = pst.executeQuery();
      while (rs.next()) {
        // Uso un método auxiliar para convertir cada fila del ResultSet en un objeto Reparacion.
        lista.add(mapResultSetToReparacion(rs, false));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al listar reparaciones", e);
    } finally {
      // Cierro los recursos para evitar bloqueos en la base de datos.
      cerrarRecursos(rs, pst, conn);
    }
    return lista;
  }

  @Override
  public Reparacion getById(Integer id) {
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    Reparacion reparacion = null;

    try {
      // Busco una reparación específica por su ID.
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_GETBYID);
      pst.setInt(1, id);
      rs = pst.executeQuery();
      if (rs.next()) {
        // Mapeo el resultado solicitando el objeto completo (true) para tener todos los detalles.
        reparacion = mapResultSetToReparacion(rs, true);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener reparación por ID", e);
    } finally {
      cerrarRecursos(rs, pst, conn);
    }
    return reparacion;
  }

  @Override
  public void insert(Reparacion r) {
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
      // Preparo la inserción solicitando el ID generado.
      pst = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

      // Asigno las claves foráneas (IDs de equipo y técnico).
      pst.setInt(1, r.getEquipo().getIdEquipo());
      pst.setInt(2, r.getUsuario().getIdUsuario());
      pst.setString(3, r.getDiagnosticoFinal());

      // Convierto mi Enum de Java a un String para poder guardarlo en la base de datos.
      pst.setString(4, r.getEstado().name());

      // Guardo los costos usando BigDecimal para mantener la precisión monetaria.
      pst.setBigDecimal(5, r.getCostoRepuestos());
      pst.setBigDecimal(6, r.getCostoManoObra());
      pst.setBigDecimal(7, r.getPresupuestoTotal());

      // Convierto las fechas de LocalDate a java.sql.Date, manejando posibles nulos.
      pst.setDate(8, r.getFechaEntregaEstimada() != null ? Date.valueOf(r.getFechaEntregaEstimada()) : null);
      pst.setDate(9, r.getFechaDiagnostico() != null ? Date.valueOf(r.getFechaDiagnostico()) : null);

      int res = pst.executeUpdate();
      if (res == 1) {
        rs = pst.getGeneratedKeys();
        if (rs.next()) {
          // Actualizo el objeto con el ID que asignó la base de datos.
          r.setIdReparacion(rs.getInt(1));
        }
        System.out.println("Reparación insertada con ID: " + r.getIdReparacion());
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al insertar reparación", e);
    } finally {
      cerrarRecursos(rs, pst, conn);
    }
  }

  @Override
  public void update(Reparacion r) {
    Connection conn = null;
    PreparedStatement pst = null;

    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_UPDATE);

      // Actualizo todos los campos, incluyendo costos y fechas modificadas.
      pst.setInt(1, r.getUsuario().getIdUsuario());
      pst.setString(2, r.getDiagnosticoFinal());
      pst.setString(3, r.getEstado().name()); // Enum -> String
      pst.setBigDecimal(4, r.getCostoRepuestos());
      pst.setBigDecimal(5, r.getCostoManoObra());
      pst.setBigDecimal(6, r.getPresupuestoTotal());
      pst.setDate(7, r.getFechaEntregaEstimada() != null ? Date.valueOf(r.getFechaEntregaEstimada()) : null);
      pst.setDate(8, r.getFechaDiagnostico() != null ? Date.valueOf(r.getFechaDiagnostico()) : null);

      // Uso el ID en el WHERE para asegurar la integridad de la actualización.
      pst.setInt(9, r.getIdReparacion());

      pst.executeUpdate();
      System.out.println("Reparación actualizada ID: " + r.getIdReparacion());

    } catch (SQLException e) {
      throw new RuntimeException("Error al actualizar reparación", e);
    } finally {
      cerrarRecursos(null, pst, conn);
    }
  }

  @Override
  public void delete(Integer id) {
    Connection conn = null;
    PreparedStatement pst = null;
    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_DELETE);
      pst.setInt(1, id);
      pst.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Error al eliminar reparación", e);
    } finally {
      cerrarRecursos(null, pst, conn);
    }
  }

  @Override
  public boolean existsById(Integer id) {
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    boolean existe = false;
    try {
      // Verifico rápidamente la existencia del registro.
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_EXISTS);
      pst.setInt(1, id);
      rs = pst.executeQuery();
      if(rs.next()) existe = true;
    } catch (SQLException e) { throw new RuntimeException(e); }
    finally { cerrarRecursos(rs, pst, conn); }
    return existe;
  }

  /**
   * Obtiene reparaciones por ID de equipo. Útil para eliminaciones en cascada.
   */
  public List<Reparacion> getByEquipoId(Integer idEquipo) {
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Reparacion> lista = new ArrayList<>();
    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_GETBY_EQUIPO);
      pst.setInt(1, idEquipo);
      rs = pst.executeQuery();
      while (rs.next()) {
        Reparacion r = new Reparacion();
        r.setIdReparacion(rs.getInt("id_reparacion"));
        lista.add(r);
      }
    } catch (SQLException e) { throw new RuntimeException(e); }
    finally { cerrarRecursos(rs, pst, conn); }
    return lista;
  }

  public List<Reparacion> getByEstado(EstadoReparacion estado) {
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Reparacion> lista = new ArrayList<>();

    try {
      // Filtro los resultados según el estado seleccionado en la vista.
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_GET_BY_ESTADO);
      pst.setString(1, estado.name()); // Importante: Paso el Enum como String
      rs = pst.executeQuery();
      while (rs.next()) {
        lista.add(mapResultSetToReparacion(rs, false));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al filtrar por estado", e);
    } finally {
      cerrarRecursos(rs, pst, conn);
    }
    return lista;
  }

  public List<Reparacion> getHistorialPorEquipo(Integer idEquipo) {
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Reparacion> lista = new ArrayList<>();

    try {
      // Obtengo el historial completo filtrando por el ID del equipo.
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_GET_HISTORIAL_POR_EQUIPO);
      pst.setInt(1, idEquipo);
      rs = pst.executeQuery();
      while (rs.next()) {
        lista.add(mapResultSetToReparacion(rs, false));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener historial del equipo", e);
    } finally {
      cerrarRecursos(rs, pst, conn);
    }
    return lista;
  }

  // --- MÉTODOS AUXILIARES ---

  /**
   * Método centralizado para convertir una fila de la BD (ResultSet) en un objeto Java (Reparacion).
   * Esto evita duplicar código de mapeo en cada consulta.
   */
  private Reparacion mapResultSetToReparacion(ResultSet rs, boolean completo) throws SQLException {
    Reparacion r = new Reparacion();
    r.setIdReparacion(rs.getInt("id_reparacion"));

    // Convierto el String de la BD de vuelta a mi Enum de Java.
    try {
      String estadoStr = rs.getString("estado");
      if(estadoStr != null) {
        r.setEstado(EstadoReparacion.valueOf(estadoStr));
      } else {
        r.setEstado(EstadoReparacion.PENDIENTE);
      }
    } catch (IllegalArgumentException e) {
      r.setEstado(EstadoReparacion.PENDIENTE); // Uso un valor seguro por defecto si falla la conversión.
    }

    r.setDiagnosticoFinal(rs.getString("diagnostico_final"));
    r.setCostoRepuestos(rs.getBigDecimal("costo_repuestos"));
    r.setCostoManoObra(rs.getBigDecimal("costo_mano_obra"));
    r.setPresupuestoTotal(rs.getBigDecimal("presupuesto_total"));

    // Convierto los Timestamps y Dates de SQL a los tipos modernos de Java Time.
    Timestamp ts = rs.getTimestamp("fecha_creacion");
    if (ts != null) r.setFechaCreacion(ts.toLocalDateTime());

    Date fd = rs.getDate("fecha_diagnostico");
    if (fd != null) r.setFechaDiagnostico(fd.toLocalDate());

    Date fe = rs.getDate("fecha_entrega_estimada");
    if (fe != null) r.setFechaEntregaEstimada(fe.toLocalDate());

    // Mapeo los objetos relacionados (Equipo y Cliente).
    Equipo e = new Equipo();
    e.setIdEquipo(rs.getInt("id_equipo"));

    if (completo) {
      // Si necesito el detalle completo, cargo todos los campos.
      e.setTipoEquipo(rs.getString("tipo_equipo"));
      e.setMarca(rs.getString("marca"));
      e.setModelo(rs.getString("modelo"));
      e.setNumeroSerie(rs.getString("num_serie"));
      e.setProblemaReportado(rs.getString("problema_reportado"));

      Cliente c = new Cliente();
      c.setIdCliente(rs.getInt("id_cliente"));
      c.setNombre(rs.getString("nombre_cliente"));
      c.setApellido(rs.getString("apellido_cliente"));
      if(hasColumn(rs, "email")) c.setEmail(rs.getString("email"));
      if(hasColumn(rs, "telefono")) c.setTelefono(rs.getString("telefono"));
      e.setCliente(c);
    } else {
      // Si es para un listado, cargo solo los datos esenciales para mejorar el rendimiento.
      if(hasColumn(rs, "tipo_equipo")) e.setTipoEquipo(rs.getString("tipo_equipo"));
      if(hasColumn(rs, "marca")) e.setMarca(rs.getString("marca"));
      if(hasColumn(rs, "modelo")) e.setModelo(rs.getString("modelo"));

      Cliente c = new Cliente();
      if(hasColumn(rs, "nombre_cliente")) c.setNombre(rs.getString("nombre_cliente"));
      if(hasColumn(rs, "apellido_cliente")) c.setApellido(rs.getString("apellido_cliente"));
      e.setCliente(c);
    }
    r.setEquipo(e);

    Usuario u = new Usuario();
    u.setIdUsuario(rs.getInt("id_usuario"));
    if(hasColumn(rs, "nombre_usuario")) u.setNombre(rs.getString("nombre_usuario"));
    r.setUsuario(u);

    return r;
  }

  private void cerrarRecursos(ResultSet rs, Statement st, Connection conn) {
    try { if (rs != null) rs.close(); } catch (Exception e) {}
    try { if (st != null) st.close(); } catch (Exception e) {}
    try { if (conn != null) conn.close(); } catch (Exception e) {}
  }

  // Método auxiliar para verificar si una columna existe en el resultado antes de leerla.
  private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
    ResultSetMetaData rsmd = rs.getMetaData();
    int columns = rsmd.getColumnCount();
    for (int x = 1; x <= columns; x++) {
      if (columnName.equals(rsmd.getColumnLabel(x))) return true;
    }
    return false;
  }
}
