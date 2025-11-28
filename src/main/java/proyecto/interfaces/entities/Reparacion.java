package proyecto.interfaces.entities;

import proyecto.interfaces.enums.EstadoReparacion; // Importar el Enum
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime; // Para el TIMESTAMP de la BD

public class Reparacion {

  private int idReparacion;
  private Equipo equipo;

  // CORRECCIÓN: Usamos 'Usuario' (singular)
  private Usuario usuario;

  private LocalDateTime fechaCreacion; // Coincide con TIMESTAMP de BD
  private LocalDate fechaDiagnostico;
  private LocalDate fechaEntregaEstimada;
  private String diagnosticoFinal;

  // CAMBIO: Ahora es Enum en lugar de String
  private EstadoReparacion estado;

  private BigDecimal costoRepuestos;
  private BigDecimal costoManoObra;
  private BigDecimal presupuestoTotal;

  public Reparacion() {}

  // Getters y Setters

  public int getIdReparacion() { return idReparacion; }
  public void setIdReparacion(int idReparacion) { this.idReparacion = idReparacion; }

  public Equipo getEquipo() { return equipo; }
  public void setEquipo(Equipo equipo) { this.equipo = equipo; }

  public Usuario getUsuario() { return usuario; }
  public void setUsuario(Usuario usuario) { this.usuario = usuario; }

  public LocalDateTime getFechaCreacion() { return fechaCreacion; }
  public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

  public LocalDate getFechaDiagnostico() { return fechaDiagnostico; }
  public void setFechaDiagnostico(LocalDate fechaDiagnostico) { this.fechaDiagnostico = fechaDiagnostico; }

  public LocalDate getFechaEntregaEstimada() { return fechaEntregaEstimada; }
  public void setFechaEntregaEstimada(LocalDate fechaEntregaEstimada) { this.fechaEntregaEstimada = fechaEntregaEstimada; }

  public String getDiagnosticoFinal() { return diagnosticoFinal; }
  public void setDiagnosticoFinal(String diagnosticoFinal) { this.diagnosticoFinal = diagnosticoFinal; }

  public EstadoReparacion getEstado() { return estado; }
  public void setEstado(EstadoReparacion estado) { this.estado = estado; }

  public BigDecimal getCostoRepuestos() { return costoRepuestos; }
  public void setCostoRepuestos(BigDecimal costoRepuestos) { this.costoRepuestos = costoRepuestos; }

  public BigDecimal getCostoManoObra() { return costoManoObra; }
  public void setCostoManoObra(BigDecimal costoManoObra) { this.costoManoObra = costoManoObra; }

  public BigDecimal getPresupuestoTotal() { return presupuestoTotal; }
  public void setPresupuestoTotal(BigDecimal presupuestoTotal) { this.presupuestoTotal = presupuestoTotal; }

  // Método útil para calcular el total antes de guardar
  public void calcularTotal() {
    BigDecimal repuestos = (this.costoRepuestos != null) ? this.costoRepuestos : BigDecimal.ZERO;
    BigDecimal manoObra = (this.costoManoObra != null) ? this.costoManoObra : BigDecimal.ZERO;
    this.presupuestoTotal = repuestos.add(manoObra);
  }

  @Override
  public String toString() {
    return "Reparacion{" +
        "id=" + idReparacion +
        ", equipo=" + (equipo != null ? equipo.getMarca() : "null") +
        ", tecnico=" + (usuario != null ? usuario.getNombre() : "null") +
        ", estado=" + estado +
        '}';
  }
}
