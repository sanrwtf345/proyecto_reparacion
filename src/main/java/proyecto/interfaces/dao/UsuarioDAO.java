package proyecto.interfaces.dao;

import proyecto.interfaces.AdminConexion;
import proyecto.interfaces.DAO;
import proyecto.interfaces.enums.RolUsuario;
import proyecto.interfaces.entities.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO implements DAO<Usuario, Integer> {

  // ELIMINADO: private Connection conn = null; -> NUNCA usar variables de clase para conexiones.

  private static final String SQL_GETALL =
      "SELECT * FROM usuarios ORDER BY id_usuario";

  private static final String SQL_INSERT =
      "INSERT INTO usuarios (correo_electronico, password, nombre, apellido, rol) " +
          "VALUES (?, ?, ?, ?, ?)";

  private static final String SQL_UPDATE =
      "UPDATE usuarios SET correo_electronico = ?, password = ?, nombre = ?, apellido = ?, rol = ? " +
          "WHERE id_usuario = ?";

  private static final String SQL_DELETE =
      "DELETE FROM usuarios WHERE id_usuario = ?";

  private static final String SQL_GETBYID =
      "SELECT * FROM usuarios WHERE id_usuario = ?";

  private static final String SQL_GETBYCORREO =
      "SELECT id_usuario, correo_electronico, password, nombre, apellido, rol " +
          "FROM usuarios WHERE correo_electronico = ?";

  private static final String SQL_GET_BY_APELLIDO =
      "SELECT * FROM usuarios WHERE apellido LIKE ? ORDER BY apellido, nombre";

  @Override
  public List<Usuario> getAll() {
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Usuario> listaUsuarios = new ArrayList<>();

    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_GETALL);
      rs = pst.executeQuery();

      while (rs.next()) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setCorreoElectronico(rs.getString("correo_electronico"));
        usuario.setPassword(rs.getString("password"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setRol(RolUsuario.valueOf(rs.getString("rol")));

        listaUsuarios.add(usuario);
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener todos los usuarios", e);
    } finally {
      cerrarRecursos(rs, pst, conn);
    }

    return listaUsuarios;
  }

  @Override
  public void insert(Usuario usuario) {
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

      pst.setString(1, usuario.getCorreoElectronico());
      pst.setString(2, usuario.getPassword());
      pst.setString(3, usuario.getNombre());
      pst.setString(4, usuario.getApellido());
      pst.setString(5, usuario.getRol().name());

      int resultado = pst.executeUpdate();
      if (resultado == 1) {
        rs = pst.getGeneratedKeys();
        if (rs.next()) {
          usuario.setIdUsuario(rs.getInt(1));
          System.out.println("Usuario insertado correctamente con ID: " + usuario.getIdUsuario());
        }
      } else {
        System.out.println("No se pudo insertar el usuario");
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al insertar usuario", e);
    } finally {
      cerrarRecursos(rs, pst, conn);
    }
  }

  @Override
  public void update(Usuario usuario) {
    if (this.existsById(usuario.getIdUsuario())) {
      Connection conn = null;
      PreparedStatement pst = null;

      try {
        conn = AdminConexion.INSTANCE.obtenerConexion();
        pst = conn.prepareStatement(SQL_UPDATE);

        pst.setString(1, usuario.getCorreoElectronico());
        pst.setString(2, usuario.getPassword());
        pst.setString(3, usuario.getNombre());
        pst.setString(4, usuario.getApellido());
        pst.setString(5, usuario.getRol().name());
        pst.setInt(6, usuario.getIdUsuario());

        int resultado = pst.executeUpdate();
        if (resultado == 1) {
          System.out.println("Usuario actualizado correctamente");
        } else {
          System.out.println("No se pudo actualizar el usuario");
        }

      } catch (SQLException e) {
        throw new RuntimeException("Error al actualizar el usuario", e);
      } finally {
        cerrarRecursos(null, pst, conn);
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
        System.out.println("Usuario eliminado correctamente");
      } else {
        System.out.println("No se pudo eliminar el usuario");
      }

    } catch (SQLException e) {
      throw new RuntimeException("No se pudo eliminar el usuario. Error: " + e.getMessage(), e);
    } finally {
      cerrarRecursos(null, pst, conn);
    }
  }

  public Usuario getByCorreoElectronico(String correoElectronico) {
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    Usuario usuario = null;

    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_GETBYCORREO);
      pst.setString(1, correoElectronico);

      rs = pst.executeQuery();

      if (rs.next()) {
        usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setCorreoElectronico(rs.getString("correo_electronico"));
        usuario.setPassword(rs.getString("password"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));

        String rolString = rs.getString("rol");
        if (rolString != null) {
          usuario.setRol(RolUsuario.valueOf(rolString.toUpperCase()));
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error en Base de Datos al buscar usuario por correo", e);
    } finally {
      cerrarRecursos(rs, pst, conn);
    }
    return usuario;
  }

  public List<Usuario> getByApellido(String apellido) {
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Usuario> listaUsuarios = new ArrayList<>();

    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_GET_BY_APELLIDO);
      pst.setString(1, "%" + apellido + "%");
      rs = pst.executeQuery();

      while (rs.next()) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setCorreoElectronico(rs.getString("correo_electronico"));
        usuario.setPassword(rs.getString("password"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setRol(RolUsuario.valueOf(rs.getString("rol")));

        listaUsuarios.add(usuario);
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al buscar usuarios por apellido", e);
    } finally {
      cerrarRecursos(rs, pst, conn);
    }

    return listaUsuarios;
  }

  @Override
  public Usuario getById(Integer id) {
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    Usuario usuario = null;

    try {
      conn = AdminConexion.INSTANCE.obtenerConexion();
      pst = conn.prepareStatement(SQL_GETBYID);
      pst.setInt(1, id);
      rs = pst.executeQuery();

      if (rs.next()) {
        usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setCorreoElectronico(rs.getString("correo_electronico"));
        usuario.setPassword(rs.getString("password"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setRol(RolUsuario.valueOf(rs.getString("rol")));
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error en Base de Datos al buscar usuario por ID", e);
    } finally {
      cerrarRecursos(rs, pst, conn);
    }

    return usuario;
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
      throw new RuntimeException("Error al verificar existencia por ID", e);
    } finally {
      cerrarRecursos(rs, pst, conn);
    }

    return existe;
  }

  // --- MÉTODO AUXILIAR ---
  private void cerrarRecursos(ResultSet rs, Statement st, Connection conn) {
    try { if (rs != null) rs.close(); } catch (Exception e) {}
    try { if (st != null) st.close(); } catch (Exception e) {}
    try { if (conn != null) conn.close(); } catch (Exception e) {}
  }
}