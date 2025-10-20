package proyecto.interfaces.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Reparacion {

  private int idReparacion;
  private Equipo equipo;
  private Usuarios usuario;
  private LocalDate fechaRecepcion;
  private LocalDate fechaDiagnostico;
  private String diagnosticoFinal;
  private String estado;
  private BigDecimal costoRepuestos;
  private BigDecimal costoManoObra;
  private BigDecimal presupuestoTotal;
  private LocalDate fechaEntregaEstimada;

  public Reparacion(){}

  public Reparacion(int idReparacion, Equipo equipo, Usuarios usuario, LocalDate fechaRecepcion, LocalDate fechaDiagnostico, String diagnosticoFinal, String estado, BigDecimal costoRepuestos, BigDecimal costoManoObra, BigDecimal presupuestoTotal, LocalDate fechaEntregaEstimada) {
    this.idReparacion = idReparacion;
    this.equipo = equipo;
    this.usuario = usuario;
    this.fechaRecepcion = fechaRecepcion;
    this.fechaDiagnostico = fechaDiagnostico;
    this.diagnosticoFinal = diagnosticoFinal;
    this.estado = estado;
    this.costoRepuestos = costoRepuestos;
    this.costoManoObra = costoManoObra;
    this.presupuestoTotal = presupuestoTotal;
    this.fechaEntregaEstimada = fechaEntregaEstimada;
  }

  //setter
  public void setFechaRecepcion(LocalDate fechaRecepcion) {
    this.fechaRecepcion = fechaRecepcion;
  }

  public void setDiagnosticoFinal(String diagnosticoFinal) {
    this.diagnosticoFinal = diagnosticoFinal;
  }

  public void setIdReparacion(int idReparacion) {
    this.idReparacion = idReparacion;
  }

  public void setEquipo(Equipo equipo) {
    this.equipo = equipo;
  }

  public void setUsuario(Usuarios usuario) {
    this.usuario = usuario;
  }

  public void setFechaDiagnostico(LocalDate fechaDiagnostico) {
    this.fechaDiagnostico = fechaDiagnostico;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public void setCostoRepuestos(BigDecimal costoRepuestos) {
    this.costoRepuestos = costoRepuestos;
  }

  public void setCostoManoObra(BigDecimal costoManoObra) {
    this.costoManoObra = costoManoObra;
  }

  public void setPresupuestoTotal(BigDecimal presupuestoTotal) {
    this.presupuestoTotal = presupuestoTotal;
  }

  public void setFechaEntregaEstimada(LocalDate fechaEntregaEstimada) {
    this.fechaEntregaEstimada = fechaEntregaEstimada;
  }

  //getters
  public LocalDate getFechaRecepcion() {
    return fechaRecepcion;
  }

  public int getIdReparacion() {
    return idReparacion;
  }

  public Equipo getEquipo() {
    return equipo;
  }

  public Usuarios getUsuario() {
    return usuario;
  }

  public LocalDate getFechaDiagnostico() {
    return fechaDiagnostico;
  }

  public String getDiagnosticoFinal() {
    return diagnosticoFinal;
  }

  public String getEstado() {
    return estado;
  }

  public BigDecimal getCostoRepuestos() {
    return costoRepuestos;
  }

  public BigDecimal getCostoManoObra() {
    return costoManoObra;
  }

  public BigDecimal getPresupuestoTotal() {
    return presupuestoTotal;
  }

  public LocalDate getFechaEntregaEstimada() {
    return fechaEntregaEstimada;
  }

  //to string
  @Override
  public String toString() {
    return "Reparacion{" +
        "idReparacion=" + idReparacion +
        ", equipo=" + equipo +
        ", tecnico=" + usuario +
        ", fechaDiagnostico=" + fechaDiagnostico +
        ", diagnosticoFinal='" + diagnosticoFinal + '\'' +
        ", estado='" + estado + '\'' +
        ", costoRepuestos=" + costoRepuestos +
        ", costoManoObra=" + costoManoObra +
        ", presupuestoTotal=" + presupuestoTotal +
        ", fechaEntregaEstimada=" + fechaEntregaEstimada +
        '}';
  }
}
