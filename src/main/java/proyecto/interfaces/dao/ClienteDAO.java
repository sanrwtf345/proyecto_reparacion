package proyecto.interfaces.dao;

import proyecto.interfaces.AdminConexion;
import proyecto.interfaces.DAO;
import proyecto.interfaces.entities.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO implements DAO<Cliente, Integer>, AdminConexion {

  private Connection conn = null;

  private static final String SQL_GETALL =
      "SELECT * FROM clientes ORDER BY apellido, nombre";

  private static final String SQL_INSERT =
      "INSERT INTO clientes (nombre, apellido, telefono, email, id_usuario) " +
          "VALUES (?, ?, ?, ?, ?)";

  private static final String SQL_UPDATE =
      "UPDATE clientes SET nombre = ?, apellido = ?, telefono = ?, email = ?, id_usuario = ? " +
          "WHERE id_cliente = ?";

  private static final String SQL_DELETE =
      "DELETE FROM clientes WHERE id_cliente = ?";

  private static final String SQL_GETBYID =
      "SELECT * FROM clientes WHERE id_cliente = ?";

  @Override
  public List<Cliente> getAll() {
    conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Cliente> listaClientes = new ArrayList<>();

    try {
      pst = conn.prepareStatement(SQL_GETALL);
      rs = pst.executeQuery();

      while (rs.next()) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setApellido(rs.getString("apellido"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEmail(rs.getString("email"));
        listaClientes.add(cliente);
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener todos los clientes", e);
    } finally {
      try {
        if (rs != null) rs.close();
        if (pst != null) pst.close();
        if (conn != null) conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    return listaClientes;
  }

  @Override
  public void insert(Cliente cliente) {
    conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;

    try {
      pst = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

      pst.setString(1, cliente.getNombre());
      pst.setString(2, cliente.getApellido());
      pst.setString(3, cliente.getTelefono());
      pst.setString(4, cliente.getEmail());
      pst.setInt(5, cliente.getUsuario().getIdUsuario());

      int resultado = pst.executeUpdate();
      if (resultado == 1) {
        rs = pst.getGeneratedKeys();
        if (rs.next()) {
          cliente.setIdCliente(rs.getInt(1));
        }
        System.out.println("Cliente insertado correctamente con id: " + cliente.getIdCliente());
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al insertar cliente", e);
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
  public void update(Cliente cliente) {
    conn = obtenerConexion();
    PreparedStatement pst = null;

    try {
      pst = conn.prepareStatement(SQL_UPDATE);

      pst.setString(1, cliente.getNombre());
      pst.setString(2, cliente.getApellido());
      pst.setString(3, cliente.getTelefono());
      pst.setString(4, cliente.getEmail());
      pst.setInt(5, cliente.getUsuario().getIdUsuario());
      pst.setInt(6, cliente.getIdCliente());

      int resultado = pst.executeUpdate();
      if (resultado == 1) {
        System.out.println("Cliente actualizado correctamente");
      } else {
        System.out.println("No se encontró el cliente para actualizar");
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al actualizar cliente", e);
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
    conn = obtenerConexion();
    PreparedStatement pst = null;

    try {
      pst = conn.prepareStatement(SQL_DELETE);
      pst.setInt(1, id);

      int resultado = pst.executeUpdate();
      if (resultado == 1) {
        System.out.println("Cliente eliminado correctamente");
      } else {
        System.out.println("No se encontró el cliente para eliminar");
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al eliminar cliente", e);
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
  public Cliente getById(Integer id) {
    conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    Cliente cliente = null;

    try {
      pst = conn.prepareStatement(SQL_GETBYID);
      pst.setInt(1, id);
      rs = pst.executeQuery();

      if (rs.next()) {
        cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setApellido(rs.getString("apellido"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEmail(rs.getString("email"));
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener cliente por id", e);
    } finally {
      try {
        if (rs != null) rs.close();
        if (pst != null) pst.close();
        if (conn != null) conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    return cliente;
  }

  @Override
  public boolean existsById(Integer id) {
    conn = obtenerConexion();
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
      throw new RuntimeException("Error al verificar existencia de cliente", e);
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


