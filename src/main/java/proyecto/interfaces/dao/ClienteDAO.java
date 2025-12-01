package proyecto.interfaces.dao;

import proyecto.interfaces.AdminConexion;
import proyecto.interfaces.DAO;
import proyecto.interfaces.entities.Cliente;
import proyecto.interfaces.entities.Usuario; // Asegúrate de importar tu clase Usuarios

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO implements DAO<Cliente, Integer>, AdminConexion {

  // Defino mis constantes SQL para mantener el código limpio y fácil de mantener
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

  private static final String SQL_GET_BY_APELLIDO =
      "SELECT * FROM clientes WHERE apellido LIKE ? ORDER BY apellido, nombre";


  @Override
  public List<Cliente> getAll() {
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Cliente> listaClientes = new ArrayList<>();

    try {
      // Ejecuto la consulta para traer todos los clientes
      pst = conn.prepareStatement(SQL_GETALL);
      rs = pst.executeQuery();

      while (rs.next()) {
        // Por cada fila, creo un objeto Cliente y lo lleno con los datos
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setApellido(rs.getString("apellido"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEmail(rs.getString("email"));

        // Aquí reconstruyo la relación con Usuario (Técnico) usando solo el ID
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        cliente.setUsuario(usuario);

        listaClientes.add(cliente);
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener todos los clientes", e);
    } finally {
      // Cierro los recursos para evitar fugas de memoria
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
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;

    try {
      // Preparo la inserción solicitando que me devuelva la clave generada (ID)
      pst = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

      pst.setString(1, cliente.getNombre());
      pst.setString(2, cliente.getApellido());
      pst.setString(3, cliente.getTelefono());
      pst.setString(4, cliente.getEmail());
      // Asocio el cliente al técnico que lo está registrando
      pst.setInt(5, cliente.getUsuario().getIdUsuario());

      int resultado = pst.executeUpdate();

      // Si se insertó correctamente, recupero el ID y actualizo mi objeto
      if (resultado == 1) {
        rs = pst.getGeneratedKeys();
        if (rs.next()) {
          cliente.setIdCliente(rs.getInt(1));
        }
        System.out.println("Cliente insertado correctamente con id: " + cliente.getIdCliente());
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al insertar cliente", e);
    } catch (NullPointerException e) {
      // Valido que el objeto Usuario no sea nulo antes de insertar
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
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;

    try {
      pst = conn.prepareStatement(SQL_UPDATE);

      // Asigno los nuevos valores a la sentencia SQL
      pst.setString(1, cliente.getNombre());
      pst.setString(2, cliente.getApellido());
      pst.setString(3, cliente.getTelefono());
      pst.setString(4, cliente.getEmail());
      pst.setInt(5, cliente.getUsuario().getIdUsuario());

      // Uso el ID en el WHERE para asegurar que modifico el correcto
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
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;

    try {
      pst = conn.prepareStatement(SQL_DELETE);
      pst.setInt(1, id);

      // Ejecuto el borrado físico del registro
      int resultado = pst.executeUpdate();
      if (resultado == 1) {
        System.out.println("Cliente eliminado correctamente");
      } else {
        System.out.println("No se encontró el cliente para eliminar");
      }

    } catch (SQLException e) {
      // Si falla (ej. tiene equipos asociados), lanzo la excepción para manejarla arriba
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
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    Cliente cliente = null;

    try {
      // Busco un cliente específico por su ID
      pst = conn.prepareStatement(SQL_GETBYID);
      pst.setInt(1, id);
      rs = pst.executeQuery();

      if (rs.next()) {
        // Si lo encuentro, mapeo los datos al objeto
        cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setApellido(rs.getString("apellido"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEmail(rs.getString("email"));

        Usuario usuario = new Usuario();
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

  // Método especial para el buscador
  public List<Cliente> getByApellido(String apellido) {
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Cliente> listaClientes = new ArrayList<>();

    try {
      pst = conn.prepareStatement(SQL_GET_BY_APELLIDO);
      // Uso comodines (%) para permitir búsquedas parciales
      pst.setString(1, "%" + apellido + "%");
      rs = pst.executeQuery();

      while (rs.next()) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        // ... (mapeo los datos igual que en getAll)
        cliente.setNombre(rs.getString("nombre"));
        cliente.setApellido(rs.getString("apellido"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEmail(rs.getString("email"));

        listaClientes.add(cliente);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al buscar clientes por apellido", e);
    } finally {
      try { if (rs != null) rs.close(); if (pst != null) pst.close(); if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
    return listaClientes;
  }

  @Override
  public boolean existsById(Integer id) {
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    boolean existe = false;

    try {
      // Verifico rápidamente si el ID existe en la base de datos
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


