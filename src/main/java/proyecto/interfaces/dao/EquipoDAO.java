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

  private Connection conn = null;

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
      "DELETE FROM usuarios WHERE id_equipo = ?";

  private static final String SQL_GETBYID =
      "SELECT * FROM equipo WHERE id_equipo = ?";

  @Override
  public List<Equipo> getAll() {
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Equipo> listaEquipos = new ArrayList<>();

    try {
      pst = conn.prepareStatement(SQL_GETALL);
      rs = pst.executeQuery();

      while (rs.next()) {

        //CONSTRUIR EL OBJETO USUARIO (TÉCNICO) ASOCIADO AL CLIENTE (solo ID necesario)
        Usuarios usuarioAsociado = new Usuarios();
        usuarioAsociado.setIdUsuario(rs.getInt("id_usuario"));

        //CONSTRUIR EL OBJETO CLIENTE (DUEÑO DEL EQUIPO)
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNombre(rs.getString("nombre_cliente"));
        cliente.setApellido(rs.getString("apellido_cliente"));
        cliente.setTelefono(rs.getString("telefono_cliente"));
        cliente.setEmail(rs.getString("email_cliente"));
        cliente.setUsuario(usuarioAsociado); // Asignar el técnico asociado

        //CONSTRUIR EL OBJETO EQUIPO
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
      // Cierre de recursos (debes asegurarte de tener un helper para cerrar o usar try-with-resources)
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
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;

    try {
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

  public Integer insertWithIdReturn(Equipo equipo) {
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    Integer generatedId = null; // Inicializamos a null, que es lo que devolveremos en caso de fallo

    try {
      // MUY IMPORTANTE: Usar Statement.RETURN_GENERATED_KEYS
      pst = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

      // 1. Setear los parámetros (basado en tu SQL_INSERT de EquipoDAO)
      pst.setInt(1, equipo.getCliente().getIdCliente());
      pst.setString(2, equipo.getTipoEquipo());
      pst.setString(3, equipo.getMarca());
      pst.setString(4, equipo.getModelo());
      pst.setString(5, equipo.getNumeroSerie());
      pst.setString(6, equipo.getProblemaReportado()); // Mapeado a problema_reportado

      int resultado = pst.executeUpdate();

      if (resultado == 1) {
        // 2. Obtener el ID generado
        rs = pst.getGeneratedKeys();
        if (rs.next()) {
          generatedId = rs.getInt(1); // Capturamos el ID
          equipo.setIdEquipo(generatedId); // Opcional: seteamos el ID en el objeto
        }
        System.out.println("Equipo insertado con ID: " + equipo.getIdEquipo());
      }

    } catch (SQLException e) {
      System.err.println("Error al insertar equipo y obtener ID: " + e.getMessage());
      throw new RuntimeException("Error en Base de Datos al insertar equipo", e);
    } finally {
      // 3. Cierre de recursos (debes asegurarte de que estén correctamente definidos)
      try {
        if (rs != null) rs.close();
        if (pst != null) pst.close();
        if (conn != null) conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    // 4. Devolver el ID
    return generatedId;
  }

  @Override
  public void update(Equipo equipo) {
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;

    try {
      pst = conn.prepareStatement(SQL_UPDATE);

      // 1. Asignar nuevos valores a los campos
      pst.setString(1, equipo.getTipoEquipo());
      pst.setString(2, equipo.getMarca());
      pst.setString(3, equipo.getModelo());
      pst.setString(4, equipo.getNumeroSerie());
      pst.setString(5, equipo.getProblemaReportado());

      // 2. Establecer la condición WHERE (el ID del equipo)
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
      // Asegúrate de cerrar los recursos
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
    try {
      PreparedStatement pst = conn.prepareStatement(SQL_DELETE);
      pst.setInt(1, id);

      int resultado = pst.executeUpdate();
      if (resultado == 1) {
        System.out.println("Equipo eliminado correctamente");
      } else {
        System.out.println("No se pudo eliminar el equipo");
      }

      pst.close();
      conn.close();

    } catch (SQLException e) {
      System.out.println("No se pudo eliminar el equipo. Error: " + e.getMessage());
      throw new RuntimeException(e);
    }

  }

  @Override
  public Equipo getById(Integer id) {
    conn = obtenerConexion();
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
        equipo.setProblemaReportado(rs.getString("problema_reportado"));
      }

      rs.close();
      pst.close();
      conn.close();

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return equipo;
  }

  @Override
  public boolean existsById(Integer id) {conn = obtenerConexion();
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

      rs.close();
      pst.close();
      conn.close();

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return existe;
  }
}
