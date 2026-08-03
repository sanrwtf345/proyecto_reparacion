package proyecto.interfaces.entities;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

class ReparacionTest {

  @Test
  void deberia_CalcularTotalCorrectamente_Cuando_AmbosCostosExisten() {
    // 1. Arrange (Preparar el escenario)
    Reparacion reparacion = new Reparacion();
    reparacion.setCostoRepuestos(new BigDecimal("15000.50"));
    reparacion.setCostoManoObra(new BigDecimal("10000.00"));

    // 2. Act (Actuar - Ejecutar la lógica a probar)
    reparacion.calcularTotal();

    // 3. Assert (Verificar el resultado con AssertJ)
    assertThat(reparacion.getPresupuestoTotal())
        .as("La suma de repuestos y mano de obra debe ser exacta")
        .isEqualByComparingTo(new BigDecimal("25000.50"));
  }

  @Test
  void deberia_AsignarTotalCorrecto_Cuando_SoloHayCostoDeManoDeObra() {
    // 1. Arrange
    Reparacion reparacion = new Reparacion();
    reparacion.setCostoRepuestos(BigDecimal.ZERO);
    reparacion.setCostoManoObra(new BigDecimal("8500.00"));

    // 2. Act
    reparacion.calcularTotal();

    // 3. Assert
    assertThat(reparacion.getPresupuestoTotal())
        .as("Si los repuestos son 0, el total debe ser igual a la mano de obra")
        .isEqualByComparingTo(new BigDecimal("8500.00"));
  }

  @Test
  void deberia_AsignarCeroAlTotal_Cuando_AmbosCostosSonNulos() {
    // 1. Arrange
    Reparacion reparacion = new Reparacion();
    // Simulamos un escenario donde los costos no se inicializaron correctamente
    reparacion.setCostoRepuestos(null);
    reparacion.setCostoManoObra(null);

    // 2. Act
    reparacion.calcularTotal();

    // 3. Assert
    assertThat(reparacion.getPresupuestoTotal())
        .as("Si ambos costos son nulos, el presupuesto total debe calcularse como 0 para evitar errores")
        .isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void deberia_ManejarCostosNegativos_Cuando_SeIngresanPorError() {
    // 1. Arrange
    Reparacion reparacion = new Reparacion();
    reparacion.setCostoRepuestos(new BigDecimal("-5000.00"));
    reparacion.setCostoManoObra(new BigDecimal("10000.00"));

    // 2. Act
    reparacion.calcularTotal();

    // 3. Assert
    // Aquí validamos cómo tu sistema maneja valores negativos.
    // Si calcularTotal() hace una suma directa, el total será 5000.
    assertThat(reparacion.getPresupuestoTotal())
        .isEqualByComparingTo(new BigDecimal("5000.00"));
  }
}