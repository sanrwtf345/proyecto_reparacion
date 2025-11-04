package proyecto.interfaces.utils;

// Importamos la clase que descargamos con Maven
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Clase de utilidad para manejar el hashing de contraseñas usando BCrypt.
 * Basado en el Paso 8 (Página 14) del PDF.
 */
public class PasswordUtil {

  // 1. Instancia estática del codificador
  // [cite: 188]
  private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  /**
   * Codifica una contraseña en texto plano a un hash BCrypt.
   *
   * @param plainPassword La contraseña en texto plano (ej: "1234")
   * @return Un hash seguro (ej: "$2a$10$...")
   */
  public static String hashPassword(String plainPassword) {
    return encoder.encode(plainPassword);
  }

  /**
   * Verifica si una contraseña en texto plano coincide con un hash guardado.
   *
   * @param plainPassword La contraseña ingresada por el usuario (ej: "1234")
   * @param hashedPassword El hash guardado en la base de datos (ej: "$2a$10$...")
   * @return true si las contraseñas coinciden, false en caso contrario.
   */
  public static boolean verifyPassword(String plainPassword, String hashedPassword) {
    return encoder.matches(plainPassword, hashedPassword);
  }
}
