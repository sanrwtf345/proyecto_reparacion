package proyecto.interfaces.entities;

import org.junit.jupiter.api.Test;
import proyecto.interfaces.enums.RolUsuario;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

  @Test
  void deberia_RetornarNegativo_Cuando_ElPrimerApellidoEsAlfabeticamenteMenor() {
    // 1. Arrange (Preparar)
    // "Alvarez" viene antes que "Zeta" en el alfabeto
    Usuario usuario1 = new Usuario("a@mail.com", "123", "Juan", "Alvarez", RolUsuario.TECNICO);
    Usuario usuario2 = new Usuario("z@mail.com", "123", "Pedro", "Zeta", RolUsuario.TECNICO);

    // 2. Act (Actuar)
    int resultado = usuario1.compareTo(usuario2);

    // 3. Assert (Verificar)
    // Si el primero es "menor", compareTo debe devolver un número negativo
    assertThat(resultado).isNegative();
  }

  @Test
  void deberia_RetornarPositivo_Cuando_ElPrimerApellidoEsAlfabeticamenteMayor() {
    // 1. Arrange
    // "Zeta" viene después que "Alvarez"
    Usuario usuario1 = new Usuario("z@mail.com", "123", "Pedro", "Zeta", RolUsuario.TECNICO);
    Usuario usuario2 = new Usuario("a@mail.com", "123", "Juan", "Alvarez", RolUsuario.TECNICO);

    // 2. Act
    int resultado = usuario1.compareTo(usuario2);

    // 3. Assert
    assertThat(resultado).isPositive();
  }

  @Test
  void deberia_DesempatarPorNombre_Cuando_TienenMismoApellido() {
    // 1. Arrange
    // Mismo apellido ("Gomez"), pero "Ana" viene antes que "Carlos"
    Usuario usuario1 = new Usuario("ana@mail.com", "123", "Ana", "Gomez", RolUsuario.TECNICO);
    Usuario usuario2 = new Usuario("carlos@mail.com", "123", "Carlos", "Gomez", RolUsuario.TECNICO);

    // 2. Act
    int resultado = usuario1.compareTo(usuario2);

    // 3. Assert
    assertThat(resultado).isNegative();
  }

  @Test
  void deberia_RetornarCero_Cuando_TienenMismoNombreYApellido() {
    // 1. Arrange
    Usuario usuario1 = new Usuario("1@mail.com", "123", "Pablo", "Perez", RolUsuario.TECNICO);
    Usuario usuario2 = new Usuario("2@mail.com", "456", "Pablo", "Perez", RolUsuario.ADMIN);

    // 2. Act
    int resultado = usuario1.compareTo(usuario2);

    // 3. Assert
    // Si nombre y apellido son iguales, compareTo debe ser 0, sin importar el email o rol
    assertThat(resultado).isZero();
  }
}