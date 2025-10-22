package proyecto.interfaces.dao;

import proyecto.interfaces.AdminConexion;
import proyecto.interfaces.DAO;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Equipo;
import proyecto.interfaces.entities.Usuarios;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipoDAO implements DAO<Equipo, Integer>, AdminConexion {

  // Se elimina 'private Connection conn = null;' para garantizar thread-safety.

  private static final String SQL_GETALL =
      "SELECT e.*, c.nombre AS nombre_cliente, c.apellido AS apellido_cliente, " +
          "c.telefono AS telefono_cliente, c.email AS email_cliente, c.id_usuario " +
          "FROM equipo e " +
          "JOIN clientes c ON e.id_cliente = c.id_cliente " +
          "ORDER BY e.fecha_registro DESC";

  private static final String SQL_INSERT =
      "INSERT INTO equipo (id_cliente, tipo_equipo, marca, modelo, num_serie, problema_reportado) " +
          "VALUES (?, ?, ?, ?, ?, ?)";

  private static final String SQL_UPDATE =
      "UPDATE equipo SET tipo_equipo = ?, marca = ?, modelo = ?, num_serie = ?, problema_reportado = ? " +
          "WHERE id_equipo = ?";

  // ✅ CORRECCIÓN 1: El DELETE debe apuntar a la tabla 'equipo'
  private static final String SQL_DELETE =
      "DELETE FROM equipo WHERE id_equipo = ?";

  private static final String SQL_GETBYID =
      "SELECT * FROM equipo WHERE id_equipo = ?";

  // ✅ NUEVO: Query para obtener equipos por ID de cliente
  private static final String SQL_GETBYCLIENTEID =
      "SELECT id_equipo, id_cliente, tipo_equipo, marca, modelo, num_serie, problema_reportado " +
          "FROM equipo WHERE id_cliente = ?";


  @Override
  public List<Equipo> getAll() {
    Connection conn = obtenerConexion(); // ✅ Conexión local
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Equipo> listaEquipos = new ArrayList<>();

    try {
      pst = conn.prepareStatement(SQL_GETALL);
      rs = pst.executeQuery();

      while (rs.next()) {

        // Construir el objeto USUARIO (Técnico) asociado al cliente (solo ID necesario)
        Usuarios usuarioAsociado = new Usuarios();
        usuarioAsociado.setIdUsuario(rs.getInt("id_usuario"));

        // Construir el objeto CLIENTE (dueño del equipo)
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNombre(rs.getString("nombre_cliente"));
        cliente.setApellido(rs.getString("apellido_cliente"));
        cliente.setTelefono(rs.getString("telefono_cliente"));
        cliente.setEmail(rs.getString("email_cliente"));
        cliente.setUsuario(usuarioAsociado); // Asignar el técnico asociado

        // Construir el objeto EQUIPO
        Equipo equipo = new Equipo();
        equipo.setIdEquipo(rs.getInt("id_equipo"));
        equipo.setCliente(cliente); // Establecer la relación
        equipo.setTipoEquipo(rs.getString("tipo_equipo"));
        equipo.setMarca(rs.getString("marca"));
        equipo.setModelo(rs.getString("modelo"));
        equipo.setNumeroSerie(rs.getString("num_serie"));
        equipo.setProblemaReportado(rs.getString("problema_reportado"));

        listaEquipos.add(equipo);
      }

    } catch (SQLException e) {
      System.err.println("Error al obtener todos los equipos: " + e.getMessage());
      throw new RuntimeException("Error en Base de Datos al listar equipos", e);
    } finally {
      try {
        if (rs != null) rs.close();
        if (pst != null) pst.close();
        if (conn != null) conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    return listaEquipos;
  }

  /**
   * Obtiene todos los equipos asociados a un ID de Cliente.
   * Necesario para la eliminación en cascada de Cliente.
   * @param idCliente El ID del cliente a buscar.
   * @return Lista de Equipos pertenecientes a ese cliente.
   */
  public List<Equipo> getByClienteId(Integer idCliente) {
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Equipo> listaEquipos = new ArrayList<>();

    try {
      pst = conn.prepareStatement(SQL_GETBYCLIENTEID);
      pst.setInt(1, idCliente);
      rs = pst.executeQuery();

      // Creamos un placeholder de cliente para la FK, solo necesitamos el ID.
      Cliente clientePlaceholder = new Cliente();
      clientePlaceholder.setIdCliente(idCliente);

      while (rs.next()) {
        Equipo equipo = new Equipo();
        equipo.setIdEquipo(rs.getInt("id_equipo"));
        equipo.setCliente(clientePlaceholder); // Asignar el placeholder de cliente

        // Mapeo mínimo de campos, aunque para la eliminación solo necesitamos el ID
        equipo.setTipoEquipo(rs.getString("tipo_equipo"));
        equipo.setMarca(rs.getString("marca"));
        equipo.setModelo(rs.getString("modelo"));
        equipo.setNumeroSerie(rs.getString("num_serie"));
        equipo.setProblemaReportado(rs.getString("problema_reportado"));

        listaEquipos.add(equipo);
      }

    } catch (SQLException e) {
      System.err.println("Error al obtener equipos por ID de cliente: " + e.getMessage());
      throw new RuntimeException("Error en Base de Datos al listar equipos por cliente", e);
    } finally {
      try {
        if (rs != null) rs.close();
        if (pst != null) pst.close();
        if (conn != null) conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    return listaEquipos;
  }


  @Override
  public void insert(Equipo equipo) {
    Connection conn = obtenerConexion(); // ✅ Conexión local
    PreparedStatement pst = null;
    ResultSet rs = null;

    try {
      pst = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

      // Los índices 1 a 6 corresponden al SQL_INSERT
      pst.setInt(1, equipo.getCliente().getIdCliente());
      pst.setString(2, equipo.getTipoEquipo());
      pst.setString(3, equipo.getMarca());
      pst.setString(4, equipo.getModelo());
      pst.setString(5, equipo.getNumeroSerie());
      pst.setString(6, equipo.getProblemaReportado());

      int resultado = pst.executeUpdate();
      if (resultado == 1) {
        rs = pst.getGeneratedKeys();
        if (rs.next()) {
          equipo.setIdEquipo(rs.getInt(1)); // ✅ Setea el ID en el objeto Equipo
        }
        System.out.println("Equipo insertado con ID: " + equipo.getIdEquipo());
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al insertar equipo", e);
    } finally {
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
  public void update(Equipo equipo) {
    Connection conn = obtenerConexion(); // ✅ Conexión local
    PreparedStatement pst = null;

    try {
      pst = conn.prepareStatement(SQL_UPDATE);

      pst.setString(1, equipo.getTipoEquipo());
      pst.setString(2, equipo.getMarca());
      pst.setString(3, equipo.getModelo());
      pst.setString(4, equipo.getNumeroSerie());
      pst.setString(5, equipo.getProblemaReportado());
      pst.setInt(6, equipo.getIdEquipo());

      int resultado = pst.executeUpdate();
      if (resultado == 1) {
        System.out.println("Equipo con ID " + equipo.getIdEquipo() + " actualizado correctamente.");
      } else {
        System.out.println("Error: No se encontró el equipo para actualizar.");
      }

    } catch (SQLException e) {
      System.err.println("Error al actualizar el equipo: " + e.getMessage());
      throw new RuntimeException("Error en Base de Datos al actualizar equipo", e);
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
    Connection conn = obtenerConexion(); // ✅ Conexión local
    PreparedStatement pst = null;

    try {
      pst = conn.prepareStatement(SQL_DELETE);
      pst.setInt(1, id);

      int resultado = pst.executeUpdate();
      if (resultado == 1) {
        System.out.println("Equipo eliminado correctamente");
      } else {
        System.out.println("No se pudo eliminar el equipo");
      }

    } catch (SQLException e) {
      System.out.println("No se pudo eliminar el equipo. Error: " + e.getMessage());
      throw new RuntimeException(e);
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
  public Equipo getById(Integer id) {
    Connection conn = obtenerConexion(); // ✅ Conexión local
    PreparedStatement pst = null;
    ResultSet rs = null;
    Equipo equipo = null;

    try {
      pst = conn.prepareStatement(SQL_GETBYID);
      pst.setInt(1, id);
      rs = pst.executeQuery();

      if (rs.next()) {
        equipo = new Equipo();
        equipo.setIdEquipo(rs.getInt("id_equipo"));
        equipo.setTipoEquipo(rs.getString("tipo_equipo"));
        equipo.setMarca(rs.getString("marca"));
        equipo.setModelo(rs.getString("modelo"));
        equipo.setNumeroSerie(rs.getString("num_serie")); // Asumo que esta columna existe
        equipo.setProblemaReportado(rs.getString("problema_reportado"));

        // ⚠️ Nota: getById solo trae datos del equipo. Para tener el objeto Cliente completo,
        // necesitarías usar un JOIN o llamar a ClienteDAO.getById(rs.getInt("id_cliente")).
        // Por ahora, solo mapeamos la FK como ID:
        Cliente clientePlaceholder = new Cliente();
        clientePlaceholder.setIdCliente(rs.getInt("id_cliente"));
        equipo.setCliente(clientePlaceholder);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        if (rs != null) rs.close();
        if (pst != null) pst.close();
        if (conn != null) conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    return equipo;
  }

  @Override
  public boolean existsById(Integer id) {
    Connection conn = obtenerConexion(); // ✅ Conexión local
    PreparedStatement pst = null;
    ResultSet rs = null;
    boolean existe = false;

    try {
      pst = conn.prepareStatement(SQL_GETBYID);
      pst.setInt(1, id);
      rs = pst.executeQuery();

      if (rs.next()) {
        existe = true;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
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
