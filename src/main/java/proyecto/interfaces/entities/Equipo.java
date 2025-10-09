package proyecto.interfaces.entities;

public class Equipo {
  private int idEquipo;
  private Cliente cliente; // Relación: el dueño del equipo
  private String tipoEquipo; // Ej: Desktop, Laptop, Tablet, etc.
  private String marca;
  private String modelo;
  private String numeroSerie;
  private String problemaReportado; // Diagnóstico inicial del cliente



  public Equipo() {}

  public Equipo(Cliente cliente, String tipoEquipo, String marca, String modelo, String numeroSerie, String problemaReportado) {
    this.cliente = cliente;
    this.tipoEquipo = tipoEquipo;
    this.marca = marca;
    this.modelo = modelo;
    this.numeroSerie = numeroSerie;
    this.problemaReportado = problemaReportado;
  }

  //setters
  public void setIdEquipo(int idEquipo) {
    this.idEquipo = idEquipo;
  }

  public void setCliente(Cliente cliente) {
    this.cliente = cliente;
  }

  public void setTipoEquipo(String tipoEquipo) {
    this.tipoEquipo = tipoEquipo;
  }

  public void setMarca(String marca) {
    this.marca = marca;
  }

  public void setModelo(String modelo) {
    this.modelo = modelo;
  }

  public void setNumeroSerie(String numeroSerie) {
    this.numeroSerie = numeroSerie;
  }

  public void setProblemaReportado(String problemaReportado) {
    this.problemaReportado = problemaReportado;
  }

  //getters
  public int getIdEquipo() {
    return idEquipo;
  }

  public Cliente getCliente() {
    return cliente;
  }

  public String getTipoEquipo() {
    return tipoEquipo;
  }

  public String getMarca() {
    return marca;
  }

  public String getModelo() {
    return modelo;
  }

  public String getNumeroSerie() {
    return numeroSerie;
  }

  public String getProblemaReportado() {
    return problemaReportado;
  }

  //to string

  @Override
  public String toString() {
    return "Equipo{" +
        "idEquipo=" + idEquipo +
        ", cliente=" + cliente +
        ", tipoEquipo='" + tipoEquipo + '\'' +
        ", marca='" + marca + '\'' +
        ", modelo='" + modelo + '\'' +
        ", numeroSerie='" + numeroSerie + '\'' +
        ", problemaReportado='" + problemaReportado + '\'' +
        '}';
  }
}
