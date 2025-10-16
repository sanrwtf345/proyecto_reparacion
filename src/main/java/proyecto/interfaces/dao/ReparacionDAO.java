package proyecto.interfaces.dao;

import proyecto.interfaces.AdminConexion;
import proyecto.interfaces.DAO;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Equipo;
import proyecto.interfaces.entities.Reparacion;
import proyecto.interfaces.entities.Usuarios;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReparacionDAO implements DAO<Reparacion, Integer>, AdminConexion {

  private Connection conn = null;

  private static final String SQL_INSERT =
      "INSERT INTO reparacion (id_equipo, id_usuario, diagnostico_final, estado, costo_repuestos, costo_mano_obra, presupuesto_total) " +
          "VALUES (?, ?, ?, ?, ?, ?, ?)";

  private static final String SQL_UPDATE =
      "UPDATE reparacion SET id_equipo = ?, id_usuario = ?, fecha_diagnostico = ?, " +
          "diagnostico_final = ?, estado = ?, costo_repuestos = ?, costo_mano_obra = ?, presupuesto_total = ?, fecha_entrega_estimada = ? " +
          "WHERE id_reparacion = ?";

  private static final String SQL_GETALL =
      "SELECT r.*, e.tipo_equipo, c.nombre AS nombre_cliente, c.apellido AS apellido_cliente, " +
          "u.nombre AS nombre_usuario, u.apellido AS apellido_usuario " +
          "FROM reparacion r " +
          "JOIN equipo e ON r.id_equipo = e.id_equipo " +
          "JOIN clientes c ON e.id_cliente = c.id_cliente " +
          "JOIN usuarios u ON r.id_usuario = u.id_usuario " +
          "ORDER BY r.fecha_creacion DESC";

  private static final String SQL_GETBYID =
      "SELECT r.*, e.tipo_equipo, e.marca, e.modelo, e.num_serie, e.problema_reportado, " +
          "c.id_cliente, c.nombre AS nombre_cliente, c.apellido AS apellido_cliente, c.telefono, c.email, " +
          "u.id_usuario, u.nombre AS nombre_usuario, u.apellido AS apellido_usuario " +
          "FROM reparacion r " +
          "JOIN equipo e ON r.id_equipo = e.id_equipo " +
          "JOIN clientes c ON e.id_cliente = c.id_cliente " +
          "JOIN usuarios u ON r.id_usuario = u.id_usuario " +
          "WHERE r.id_reparacion = ?";

  private static final String SQL_EXISTSBYID =
      "SELECT COUNT(*) FROM reparacion WHERE id_reparacion = ?";

  private static final String SQL_DELETE =
      "DELETE FROM reparacion WHERE id_reparacion = ?";

  @Override
  public List<Reparacion> getAll() {
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Reparacion> listaReparaciones = new ArrayList<>();

    try {
      pst = conn.prepareStatement(SQL_GETALL);
      rs = pst.executeQuery();

      while (rs.next()) {

        Usuarios usuario = new Usuarios();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombre(rs.getString("nombre_usuario"));
        usuario.setApellido(rs.getString("apellido_usuario"));

        Cliente cliente = new Cliente();
        cliente.setNombre(rs.getString("nombre_cliente"));
        cliente.setApellido(rs.getString("apellido_cliente"));

        Equipo equipo = new Equipo();
        equipo.setIdEquipo(rs.getInt("id_equipo"));
        equipo.setTipoEquipo(rs.getString("tipo_equipo"));
        equipo.setCliente(cliente);


        Reparacion reparacion = new Reparacion();
        reparacion.setIdReparacion(rs.getInt("id_reparacion"));
        reparacion.setEquipo(equipo);
        reparacion.setUsuario(usuario);

        reparacion.setEstado(rs.getString("estado"));

        reparacion.setCostoRepuestos(rs.getBigDecimal("costo_repuestos"));
        reparacion.setCostoManoObra(rs.getBigDecimal("costo_mano_obra"));
        reparacion.setPresupuestoTotal(rs.getBigDecimal("presupuesto_total"));

        Date sqlDate = rs.getDate("fecha_diagnostico");
        if (sqlDate != null) {
          reparacion.setFechaDiagnostico(sqlDate.toLocalDate());
        }

        listaReparaciones.add(reparacion);
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener todas las reparaciones", e);
    } finally {
      try {
        if (rs != null) rs.close();
        if (pst != null) pst.close();
        if (conn != null) conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    return listaReparaciones;
  }

  @Override
  public void insert(Reparacion reparacion) {
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;

    try {
      pst = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

      // 1. Claves Foráneas
      pst.setInt(1, reparacion.getEquipo().getIdEquipo());    // ID del equipo asociado
      pst.setInt(2, reparacion.getUsuario().getIdUsuario());  // ID del técnico a cargo

      // 2. Diagnóstico y Estado Iniciales
      pst.setString(3, reparacion.getDiagnosticoFinal()); // Inicialmente podría ser null/vacío
      pst.setString(4, reparacion.getEstado());

      // 3. CAMPOS DE COSTO (BigDecimal)
      pst.setBigDecimal(5, reparacion.getCostoRepuestos());
      pst.setBigDecimal(6, reparacion.getCostoManoObra());
      pst.setBigDecimal(7, reparacion.getPresupuestoTotal());

      int resultado = pst.executeUpdate();
      if (resultado == 1) {
        rs = pst.getGeneratedKeys();
        if (rs.next()) {
          reparacion.setIdReparacion(rs.getInt(1));
        }
        System.out.println("Reparación N° " + reparacion.getIdReparacion() + " iniciada exitosamente.");
      }

    } catch (SQLException e) {
      System.err.println("Error al iniciar la reparación.");
      throw new RuntimeException("Error en Base de Datos al insertar reparación", e);
    } finally {
      // Bloque de cierre de recursos
      try {
        if (rs != null) rs.close();
        if (pst != null) pst.close();
        if (conn != null) conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void update(Reparacion reparacion) {
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;

    try {
      pst = conn.prepareStatement(SQL_UPDATE);

      // 1. Claves Foráneas
      pst.setInt(1, reparacion.getEquipo().getIdEquipo());
      pst.setInt(2, reparacion.getUsuario().getIdUsuario());

      // 2. Diagnóstico y Estado
      pst.setDate(3, (reparacion.getFechaDiagnostico() != null) ? Date.valueOf(reparacion.getFechaDiagnostico()) : null);
      pst.setString(4, reparacion.getDiagnosticoFinal());
      pst.setString(5, reparacion.getEstado());

      // 3. CAMPOS DE COSTO (BigDecimal)
      pst.setBigDecimal(6, reparacion.getCostoRepuestos());
      pst.setBigDecimal(7, reparacion.getCostoManoObra());
      pst.setBigDecimal(8, reparacion.getPresupuestoTotal()); // El total

      // 4. Fechas
      pst.setDate(9, (reparacion.getFechaEntregaEstimada() != null) ? Date.valueOf(reparacion.getFechaEntregaEstimada()) : null);

      // 5. Cláusula WHERE
      pst.setInt(10, reparacion.getIdReparacion());

      int resultado = pst.executeUpdate();
      if (resultado == 1) {
        System.out.println("Reparación N° " + reparacion.getIdReparacion() + " actualizada (incluyendo costos).");
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al actualizar la reparación", e);
    } finally {
      try {
        if (pst != null) pst.close();
        if (conn != null) conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void delete(Integer id) {
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;

    try {
      pst = conn.prepareStatement(SQL_DELETE);
      pst.setInt(1, id); // Solo necesita el ID

      int resultado = pst.executeUpdate();
      if (resultado == 1) {
        System.out.println("Reparación N° " + id + " eliminada correctamente.");
      } else {
        System.out.println("No se encontró la reparación para eliminar.");
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al eliminar la reparación. Verifique si tiene datos relacionados.", e);
    } finally {
      try {
        if (pst != null) pst.close();
        if (conn != null) conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public Reparacion getById(Integer id) {
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    Reparacion reparacion = null;

    try {
      pst = conn.prepareStatement(SQL_GETBYID);
      pst.setInt(1, id);
      rs = pst.executeQuery();

      if (rs.next()) {
        // 1. Construir Usuarios (Técnico)
        Usuarios usuario = new Usuarios();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombre(rs.getString("nombre_usuario"));
        usuario.setApellido(rs.getString("apellido_usuario"));

        // 2. Construir Cliente (Detalle completo)
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNombre(rs.getString("nombre_cliente"));
        cliente.setApellido(rs.getString("apellido_cliente"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEmail(rs.getString("email"));

        // 3. Construir Equipo (Detalle completo)
        Equipo equipo = new Equipo();
        equipo.setIdEquipo(rs.getInt("id_equipo"));
        equipo.setCliente(cliente);
        equipo.setTipoEquipo(rs.getString("tipo_equipo"));
        equipo.setMarca(rs.getString("marca"));
        equipo.setModelo(rs.getString("modelo"));
        equipo.setNumeroSerie(rs.getString("num_serie"));
        equipo.setProblemaReportado(rs.getString("problema_reportado"));

        // 4. Construir Reparacion (Detalle completo)
        reparacion = new Reparacion();
        reparacion.setIdReparacion(rs.getInt("id_reparacion"));
        reparacion.setEquipo(equipo);
        reparacion.setUsuario(usuario);

        reparacion.setDiagnosticoFinal(rs.getString("diagnostico_final"));
        reparacion.setEstado(rs.getString("estado"));

        // **NUEVO Mapeo de Costos (usando BigDecimal)**
        reparacion.setCostoRepuestos(rs.getBigDecimal("costo_repuestos"));
        reparacion.setCostoManoObra(rs.getBigDecimal("costo_mano_obra"));
        reparacion.setPresupuestoTotal(rs.getBigDecimal("presupuesto_total"));

        // Mapeo de Fechas
        Date fechaDiag = rs.getDate("fecha_diagnostico");
        if (fechaDiag != null) reparacion.setFechaDiagnostico(fechaDiag.toLocalDate());

        Date fechaEntrega = rs.getDate("fecha_entrega_estimada");
        if (fechaEntrega != null) reparacion.setFechaEntregaEstimada(fechaEntrega.toLocalDate());
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener reparación por ID", e);
    } finally {
      try {
        if (rs != null) rs.close();
        if (pst != null) pst.close();
        if (conn != null) conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return reparacion;
  }

  @Override
  public boolean existsById(Integer id) {
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    boolean existe = false;

    try {
      pst = conn.prepareStatement(SQL_EXISTSBYID);
      pst.setInt(1, id);
      rs = pst.executeQuery();

      if (rs.next()) {
        // Si el conteo es mayor que 0, la reparación existe.
        if (rs.getInt(1) > 0) {
          existe = true;
        }
      }

    } catch (SQLException e) {
      System.err.println("Error al verificar existencia de reparación por ID: " + e.getMessage());
      // En caso de error de conexión, asumimos que no existe o lanzamos la excepción
      throw new RuntimeException("Error en Base de Datos al verificar existencia", e);
    } finally {
      // Cierre de recursos
      try {
        if (rs != null) rs.close();
        if (pst != null) pst.close();
        if (conn != null) conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return existe;
  }
}
