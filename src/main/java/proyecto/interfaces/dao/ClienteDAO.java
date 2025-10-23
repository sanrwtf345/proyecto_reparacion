package proyecto.interfaces.dao;

import proyecto.interfaces.AdminConexion;
import proyecto.interfaces.DAO;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Usuarios; // Asegúrate de importar tu clase Usuarios

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO implements DAO<Cliente, Integer>, AdminConexion {

  private static final String SQL_GETALL =
      "SELECT id_cliente, nombre, apellido, telefono, email, id_usuario FROM clientes ORDER BY apellido, nombre";

  private static final String SQL_INSERT =
      "INSERT INTO clientes (nombre, apellido, telefono, email, id_usuario) " +
          "VALUES (?, ?, ?, ?, ?)";

  private static final String SQL_UPDATE =
      "UPDATE clientes SET nombre = ?, apellido = ?, telefono = ?, email = ?, id_usuario = ? " +
          "WHERE id_cliente = ?";

  private static final String SQL_DELETE =
      "DELETE FROM clientes WHERE id_cliente = ?";

  private static final String SQL_GETBYID =
      "SELECT id_cliente, nombre, apellido, telefono, email, id_usuario FROM clientes WHERE id_cliente = ?";

  // No es necesario modificar existsById, ya que es eficiente.


  @Override
  public List<Cliente> getAll() {
    Connection conn = obtenerConexion(); // ✅ Conexión declarada localmente
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

        // ✅ CORRECCIÓN 1: Mapear la FK id_usuario a un objeto Usuarios (solo con el ID)
        Usuarios usuario = new Usuarios();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        cliente.setUsuario(usuario);

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

  // RENOMBRADO de 'insertWithIdReturn' a 'insert', el patrón ideal.
  @Override
  public void insert(Cliente cliente) {
    Connection conn = obtenerConexion(); // ✅ Conexión declarada localmente
    PreparedStatement pst = null;
    ResultSet rs = null;

    try {
      // Se usa RETURN_GENERATED_KEYS para obtener el ID
      pst = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

      pst.setString(1, cliente.getNombre());
      pst.setString(2, cliente.getApellido());
      pst.setString(3, cliente.getTelefono());
      pst.setString(4, cliente.getEmail());
      // Se asume que cliente.getUsuario() no es null y tiene el ID
      pst.setInt(5, cliente.getUsuario().getIdUsuario());

      int resultado = pst.executeUpdate();

      if (resultado == 1) {
        rs = pst.getGeneratedKeys();
        if (rs.next()) {
          // ✅ Actualiza el objeto Cliente con el ID generado (patrón ideal)
          cliente.setIdCliente(rs.getInt(1));
        }
        System.out.println("Cliente insertado correctamente con id: " + cliente.getIdCliente());
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al insertar cliente", e);
    } catch (NullPointerException e) {
      // Captura si cliente.getUsuario() o getIdUsuario() es null/falla
      throw new RuntimeException("Error al obtener ID de usuario para insertar cliente.", e);
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
    Connection conn = obtenerConexion(); // ✅ Conexión declarada localmente
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
    Connection conn = obtenerConexion(); // ✅ Conexión declarada localmente
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
    Connection conn = obtenerConexion(); // ✅ Conexión declarada localmente
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

        // ✅ CORRECCIÓN 1: Mapear la FK id_usuario a un objeto Usuarios (solo con el ID)
        Usuarios usuario = new Usuarios();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        cliente.setUsuario(usuario);
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
    Connection conn = obtenerConexion(); // ✅ Conexión declarada localmente
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


