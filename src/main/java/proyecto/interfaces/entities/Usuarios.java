package proyecto.interfaces.entities;

import proyecto.interfaces.enums.RolUsuario;

public class Usuarios implements Comparable {
  private int idUsuario;
  private String correoElectronico; // <--- CAMBIO: de nombreUsuario a correoElectronico
  private String password;
  private String nombre;
  private String apellido;
  private RolUsuario rol;

  public Usuarios() {
  }

  // <--- CAMBIO: en el constructor
  public Usuarios(String correoElectronico, String password, String nombre, String apellido, RolUsuario rol) {
    this.correoElectronico = correoElectronico;
    this.password = password;
    this.nombre = nombre;
    this.apellido = apellido;
    this.rol = rol;
  }

  // <--- CAMBIO: en el constructor
  public Usuarios(int idUsuario, String correoElectronico, String password, String nombre, String apellido, RolUsuario rol) {
    this.idUsuario = idUsuario;
    this.correoElectronico = correoElectronico;
    this.password = password;
    this.nombre = nombre;
    this.apellido = apellido;
    this.rol = rol;
  }

  //setters
  public void setIdUsuario(int idUsuario) {
    this.idUsuario = idUsuario;
  }

  // <--- CAMBIO: getters y setters
  public void setCorreoElectronico(String correoElectronico) {
    this.correoElectronico = correoElectronico;
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

  // <--- CAMBIO: getters y setters
  public String getCorreoElectronico() {
    return correoElectronico;
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
    // <--- CAMBIO: actualizado el toString
    return "usuarios{" +
        "idUsuario=" + idUsuario +
        ", correoElectronico='" + correoElectronico + '\'' +
        ", password='" + password + '\'' +
        ", nombre='" + nombre + '\'' +
        ", apellido='" + apellido + '\'' +
        '}';
  }

  @Override
  public int compareTo(Object o) {
    Usuarios otro = (Usuarios) o;
    int comparacionApellido = this.apellido.compareTo(otro.apellido);

    if (comparacionApellido != 0) {
      return comparacionApellido;
    }

    return this.nombre.compareTo(otro.nombre);
  }
}
