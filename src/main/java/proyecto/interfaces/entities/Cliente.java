package proyecto.interfaces.entities;

public class Cliente {
  private int idCliente;
  private String nombre;
  private String apellido;
  private String telefono;
  private String email;
  private Usuarios usuario;

  public Cliente(){}

  public Cliente(int idCliente, String nombre, String apellido, String telefono, String email, Usuarios usuario) {
    this.idCliente = idCliente;
    this.nombre = nombre;
    this.apellido = apellido;
    this.telefono = telefono;
    this.email = email;
    this.usuario = usuario;
  }

  //setters
  public void setIdCliente(int idCliente) {
    this.idCliente = idCliente;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public void setApellido(String apellido) {
    this.apellido = apellido;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setUsuario(Usuarios usuario) {
    this.usuario = usuario;
  }

  //getter
  public String getNombre() {
    return nombre;
  }

  public String getApellido() {
    return apellido;
  }

  public int getIdCliente() {
    return idCliente;
  }

  public String getTelefono() {
    return telefono;
  }

  public String getEmail() {
    return email;
  }

  public Usuarios getUsuario() {
    return usuario;
  }

  //toString

  @Override
  public String toString() {
    return "Cliente{" +
        "idCliente=" + idCliente +
        ", nombre='" + nombre + '\'' +
        ", apellido='" + apellido + '\'' +
        ", telefono='" + telefono + '\'' +
        ", email='" + email + '\'' +
        ", usuario=" + usuario +
        '}';
  }
}
