package proyecto.interfaces.dao;

import proyecto.interfaces.AdminConexion;
import proyecto.interfaces.DAO;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Equipo;
import proyecto.interfaces.entities.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipoDAO implements DAO<Equipo, Integer> {

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

  private static final String SQL_DELETE =
      "DELETE FROM equipo WHERE id_equipo = ?";

  private static final String SQL_GETBYID =
      "SELECT * FROM equipo WHERE id_equipo = ?";

  private static final String SQL_GETBYCLIENTEID =
      "SELECT id_equipo, id_cliente, tipo_equipo, marca, modelo, num_serie, problema_reportado " +
          "FROM equipo WHERE id_cliente = ?";


  @Override
  public List<Equipo> getAll() {
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Equipo> listaEquipos = new ArrayList<>();

    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_GETALL);
      rs = pst.executeQuery();

      while (rs.next()) {
        Usuario usuarioAsociado = new Usuario();
        usuarioAsociado.setIdUsuario(rs.getInt("id_usuario"));

        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNombre(rs.getString("nombre_cliente"));
        cliente.setApellido(rs.getString("apellido_cliente"));
        cliente.setTelefono(rs.getString("telefono_cliente"));
        cliente.setEmail(rs.getString("email_cliente"));
        cliente.setUsuario(usuarioAsociado);

        Equipo equipo = new Equipo();
        equipo.setIdEquipo(rs.getInt("id_equipo"));
        equipo.setCliente(cliente);
        equipo.setTipoEquipo(rs.getString("tipo_equipo"));
        equipo.setMarca(rs.getString("marca"));
        equipo.setModelo(rs.getString("modelo"));
        equipo.setNumeroSerie(rs.getString("num_serie"));
        equipo.setProblemaReportado(rs.getString("problema_reportado"));

        listaEquipos.add(equipo);
      }

    } catch (SQLException e) {
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

  public List<Equipo> getByClienteId(Integer idCliente) {
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Equipo> listaEquipos = new ArrayList<>();

    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_GETBYCLIENTEID);
      pst.setInt(1, idCliente);
      rs = pst.executeQuery();

      Cliente clientePlaceholder = new Cliente();
      clientePlaceholder.setIdCliente(idCliente);

      while (rs.next()) {
        Equipo equipo = new Equipo();
        equipo.setIdEquipo(rs.getInt("id_equipo"));
        equipo.setCliente(clientePlaceholder);

        equipo.setTipoEquipo(rs.getString("tipo_equipo"));
        equipo.setMarca(rs.getString("marca"));
        equipo.setModelo(rs.getString("modelo"));
        equipo.setNumeroSerie(rs.getString("num_serie"));
        equipo.setProblemaReportado(rs.getString("problema_reportado"));

        listaEquipos.add(equipo);
      }

    } catch (SQLException e) {
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
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

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
          equipo.setIdEquipo(rs.getInt(1));
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
    Connection conn = null;
    PreparedStatement pst = null;

    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
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
    Connection conn = null;
    PreparedStatement pst = null;

    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
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
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    Equipo equipo = null;

    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_GETBYID);
      pst.setInt(1, id);
      rs = pst.executeQuery();

      if (rs.next()) {
        equipo = new Equipo();
        equipo.setIdEquipo(rs.getInt("id_equipo"));
        equipo.setTipoEquipo(rs.getString("tipo_equipo"));
        equipo.setMarca(rs.getString("marca"));
        equipo.setModelo(rs.getString("modelo"));
        equipo.setNumeroSerie(rs.getString("num_serie"));
        equipo.setProblemaReportado(rs.getString("problema_reportado"));

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
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    boolean existe = false;

    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
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