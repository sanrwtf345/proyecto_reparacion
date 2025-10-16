package proyecto.interfaces.entities;

import proyecto.interfaces.enums.RolUsuario;

public class Usuarios implements Comparable {
  private int idUsuario;
  private String nombreUsuario;
  private String password;
  private String nombre;
  private String apellido;
  private RolUsuario rol;

  public Usuarios() {
  }

  public Usuarios(String nombreUsuario, String password, String nombre, String apellido, RolUsuario rol) {
    this.nombreUsuario = nombreUsuario;
    this.password = password;
    this.nombre = nombre;
    this.apellido = apellido;
    this.rol = rol;
  }

  public Usuarios(int idUsuario, String nombreUsuario, String password, String nombre, String apellido, RolUsuario rol) {
    this.idUsuario = idUsuario;
    this.nombreUsuario = nombreUsuario;
    this.password = password;
    this.nombre = nombre;
    this.apellido = apellido;
    this.rol = rol;
  }

  //setters
  public void setIdUsuario(int idUsuario) {
    this.idUsuario = idUsuario;
  }

  public void setNombreUsuario(String nombreUsuario) {
    this.nombreUsuario = nombreUsuario;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public void setApellido(String apellido) {
    this.apellido = apellido;
  }

  public void setRol(RolUsuario rol) {
    this.rol = rol;
  }

  //getters
  public int getIdUsuario() {
    return idUsuario;
  }

  public String getNombreUsuario() {
    return nombreUsuario;
  }

  public String getPassword() {
    return password;
  }

  public String getNombre() {
    return nombre;
  }

  public String getApellido() {
    return apellido;
  }

  public RolUsuario getRol() {
    return rol;
  }

  //to string
  @Override
  public String toString() {
    return "usuarios{" +
        "idUsuario=" + idUsuario +
        ", nombreUsuario='" + nombreUsuario + '\'' +
        ", password='" + password + '\'' +
        ", nombre='" + nombre + '\'' +
        ", apellido='" + apellido + '\'' +
        '}';
  }

  @Override
  public int compareTo(Object o) {
    Usuarios otro= (Usuarios) o;
    int comparacionApellido = this.apellido.compareTo(otro.apellido);

    if (comparacionApellido != 0) {
      return comparacionApellido;
    }

    return this.nombre.compareTo(otro.nombre);
  }
}
