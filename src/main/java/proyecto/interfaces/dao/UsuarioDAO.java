package proyecto.interfaces.dao;

import proyecto.interfaces.AdminConexion;
import proyecto.interfaces.DAO;
import proyecto.interfaces.enums.RolUsuario;
import proyecto.interfaces.entities.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO implements DAO<Usuario, Integer>, AdminConexion {

  private Connection conn = null;

  private static final String SQL_GETALL =
      "SELECT * FROM usuarios ORDER BY id_usuario";

  // <--- CAMBIO: nombre_usuario -> correo_electronico
  private static final String SQL_INSERT =
      "INSERT INTO usuarios (correo_electronico, password, nombre, apellido, rol) " +
          "VALUES (?, ?, ?, ?, ?)";

  // <--- CAMBIO: nombre_usuario -> correo_electronico
  private static final String SQL_UPDATE =
      "UPDATE usuarios SET correo_electronico = ?, password = ?, nombre = ?, apellido = ?, rol = ? " +
          "WHERE id_usuario = ?";

  private static final String SQL_DELETE =
      "DELETE FROM usuarios WHERE id_usuario = ?";

  private static final String SQL_GETBYID =
      "SELECT * FROM usuarios WHERE id_usuario = ?";

  // <--- CAMBIO: Renombrada la constante y la consulta
  private static final String SQL_GETBYCORREO =
      "SELECT id_usuario, correo_electronico, password, nombre, apellido, rol " +
          "FROM usuarios WHERE correo_electronico = ?";

  private static final String SQL_GET_BY_APELLIDO =
      "SELECT * FROM usuarios WHERE apellido LIKE ? ORDER BY apellido, nombre";

  @Override
  public List<Usuario> getAll() {
    conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Usuario> listaUsuarios = new ArrayList<>();

    try {
      pst = conn.prepareStatement(SQL_GETALL);
      rs = pst.executeQuery();

      while (rs.next()) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setCorreoElectronico(rs.getString("correo_electronico")); // <--- CAMBIO
        usuario.setPassword(rs.getString("password"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setRol(RolUsuario.valueOf(rs.getString("rol"))); // Enum desde BD

        listaUsuarios.add(usuario);
      }

      rs.close();
      pst.close();
      conn.close();

    } catch (SQLException e) {
      System.out.println("Error al obtener todos los usuarios");
      throw new RuntimeException(e);
    }

    return listaUsuarios;
  }

  @Override
  public void insert(Usuario usuario) {
    conn = obtenerConexion();
    PreparedStatement pst = null;

    try {
      pst = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

      pst.setString(1, usuario.getCorreoElectronico()); // <--- CAMBIO
      pst.setString(2, usuario.getPassword());
      pst.setString(3, usuario.getNombre());
      pst.setString(4, usuario.getApellido());
      pst.setString(5, usuario.getRol().name()); // Guardar como String

      int resultado = pst.executeUpdate();
      if (resultado == 1) {
        System.out.println("Usuario insertado correctamente");
      } else {
        System.out.println("No se pudo insertar el usuario");
      }

      ResultSet rs = pst.getGeneratedKeys();
      if (rs.next()) {
        usuario.setIdUsuario(rs.getInt(1));
        System.out.println("El id asignado es: " + usuario.getIdUsuario());
      }

      rs.close();
      pst.close();
      conn.close();

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void update(Usuario usuario) {
    if (this.existsById(usuario.getIdUsuario())) {
      conn = obtenerConexion();
      PreparedStatement pst = null;

      try {
        pst = conn.prepareStatement(SQL_UPDATE);

        pst.setString(1, usuario.getCorreoElectronico()); // <--- CAMBIO
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

        pst.close();
        conn.close();

      } catch (SQLException e) {
        System.out.println("Error al actualizar el usuario");
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void delete(Integer id) {
    conn = obtenerConexion();

    try {
      PreparedStatement pst = conn.prepareStatement(SQL_DELETE);
      pst.setInt(1, id);

      int resultado = pst.executeUpdate();
      if (resultado == 1) {
        System.out.println("Usuario eliminado correctamente");
      } else {
        System.out.println("No se pudo eliminar el usuario");
      }

      pst.close();
      conn.close();

    } catch (SQLException e) {
      System.out.println("No se pudo eliminar el usuario. Error: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  // <--- CAMBIO: Renombrado el método y su parámetro
  public Usuario getByCorreoElectronico(String correoElectronico) {
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    Usuario usuario = null;

    try {
      pst = conn.prepareStatement(SQL_GETBYCORREO); // <--- CAMBIO
      pst.setString(1, correoElectronico); // <--- CAMBIO

      rs = pst.executeQuery();

      if (rs.next()) {
        usuario = new Usuario();

        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setCorreoElectronico(rs.getString("correo_electronico")); // <--- CAMBIO
        usuario.setPassword(rs.getString("password"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));

        String rolString = rs.getString("rol");
        if (rolString != null) {
          usuario.setRol(RolUsuario.valueOf(rolString.toUpperCase()));
        }
      }

    } catch (SQLException e) {
      System.err.println("Error al buscar usuario por correo: " + e.getMessage()); // <--- CAMBIO (cosmético)
      throw new RuntimeException("Error en Base de Datos al buscar usuario por correo", e); // <--- CAMBIO (cosmético)
    } finally {
      try {
        if (rs != null) rs.close();
        if (pst != null) pst.close();
        if (conn != null) conn.close();
      } catch (SQLException e) {
        e.printStackTrace();

      }
    }
    return usuario;
  }

  public List<Usuario> getByApellido(String apellido) {
    conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    List<Usuario> listaUsuarios = new ArrayList<>();

    try {
      pst = conn.prepareStatement(SQL_GET_BY_APELLIDO);
      // Los % permiten buscar coincidencias parciales (ej: "Per" encuentra "Perez")
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
      // (Aquí va tu bloque try-catch para cerrar recursos, igual que en getAll)
      try { if (rs != null) rs.close(); if (pst != null) pst.close(); if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }

    return listaUsuarios;
  }

  @Override
  public Usuario getById(Integer id) {
    // Declaración de variables inicializada fuera del try
    Connection conn = obtenerConexion();
    PreparedStatement pst = null;
    ResultSet rs = null;
    Usuario usuario = null; // Inicializado a null (valor de retorno por defecto)

    try {
      pst = conn.prepareStatement(SQL_GETBYID);
      pst.setInt(1, id);
      rs = pst.executeQuery();

      if (rs.next()) {
        // Mapeo si se encuentra el registro
        usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setCorreoElectronico(rs.getString("correo_electronico")); // <--- CAMBIO
        usuario.setPassword(rs.getString("password"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setRol(RolUsuario.valueOf(rs.getString("rol")));
      }

    } catch (SQLException e) {
      System.err.println("Error al obtener usuario por ID: " + e.getMessage());
      // Propagamos la excepción, pero usando System.err para loguear
      throw new RuntimeException("Error en Base de Datos al buscar usuario por ID", e);
    } finally {
      // ✅ Cierre de recursos SEGURO, independientemente de si hubo excepción o no
      try {
        if (rs != null) rs.close();
        if (pst != null) pst.close();
        if (conn != null) conn.close();
      } catch (SQLException e) {
        // Ignoramos el error de cierre, solo lo logueamos
        e.printStackTrace();
      }
    }

    return usuario; // Retornará el objeto Usuario o null si no se encontró o hubo un error manejado
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

      rs.close();
      pst.close();
      conn.close();

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return existe;
  }
}
