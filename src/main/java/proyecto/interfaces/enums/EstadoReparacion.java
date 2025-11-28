package proyecto.interfaces.enums;

public enum EstadoReparacion {
  PENDIENTE,      // Recibido, esperando diagnóstico o reparación
  EN_PROCESO,     // (Opcional, pero recomendado) Técnico trabajando
  FINALIZADO,     // Reparación técnica terminada, listo para entregar
  TERMINADO,      // Entregado al cliente / Cerrado
  CANCELADO       // Por si el cliente rechaza el presupuesto
}
