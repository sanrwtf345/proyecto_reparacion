package proyecto.interfaces.filters;

import proyecto.interfaces.entities.Usuarios;
import proyecto.interfaces.enums.RolUsuario;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebFilter("/*") // Aplica el filtro a todas las URLs del proyecto
public class AuthFilter implements Filter {

  // --- Listas de Rutas Mejoradas ---

  // Rutas que NO requieren iniciar sesión
  private static final List<String> RUTAS_PUBLICAS = Arrays.asList(
      "/login.jsp",
      "/LoginServlet",
      "/LogoutServlet"
  );

  // Rutas de recursos estáticos (CSS, JS, Imágenes)
  private static final List<String> RUTAS_ESTATICAS = Arrays.asList(
      "/css/",
      "/js/",
      "/images/",
      "/img/" // Añadido por si acaso
  );

  // Rutas que SÓLO el ADMIN puede acceder
  private static final List<String> RUTAS_ADMIN = Arrays.asList(
      "/vistas/admin/",
      "/UsuariosController" // ¡IMPORTANTE! Proteger el Servlet de Usuarios
  );

  // Rutas que ADMIN o TÉCNICO pueden acceder
  private static final List<String> RUTAS_TECNICO = Arrays.asList(
      "/vistas/tecnico/",
      "/ClienteController",
      "/EquipoController",
      "/ReparacionController",
      "/ClienteEquipoController"
      // (Verifica que los nombres de tus servlets de técnico sean correctos)
  );

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Inicialización, si es necesario
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;
    HttpSession session = req.getSession(false); // No crear sesión si no existe

    String path = req.getRequestURI().substring(req.getContextPath().length());

    // 1. Comprobar si es un recurso estático (CSS, JS, etc.)
    boolean esEstatico = RUTAS_ESTATICAS.stream().anyMatch(path::startsWith);
    if (esEstatico) {
      chain.doFilter(request, response);
      return;
    }

    // 2. Comprobar si la ruta es pública
    boolean esPublica = RUTAS_PUBLICAS.stream().anyMatch(path::equals);
    Usuarios usuarioLogueado = (session != null) ? (Usuarios) session.getAttribute("usuarioLogueado") : null;


    // =========================================================
    // LÓGICA DE CONTROL DE ACCESO
    // =========================================================

    // CASO 1: Acceso a una ruta pública (o la raíz)
    if (esPublica || path.equals("/")) {

      // MEJORA: Si es pública PERO ya está logueado, redirigir a su menú
      // (Implementación de la lógica del PDF )
      if (usuarioLogueado != null && path.equals("/login.jsp")) {
        if (usuarioLogueado.getRol() == RolUsuario.ADMIN) {
          res.sendRedirect(req.getContextPath() + "/vistas/admin/menuAdmin.jsp");
        } else {
          res.sendRedirect(req.getContextPath() + "/vistas/tecnico/menuTecnico.jsp");
        }
        return;
      }

      // Si no está logueado y va al login, permitir.
      chain.doFilter(request, response);
      return;
    }

    // CASO 2: No es ruta pública y NO está logueado
    // (Implementación centralizada de la lógica del PDF [cite: 93])
    if (usuarioLogueado == null) {
      res.sendRedirect(req.getContextPath() + "/login.jsp");
      return;
    }

    // CASO 3: Está logueado. Verificar autorización por Rol
    RolUsuario rol = usuarioLogueado.getRol();

    // 3.1: Proteger rutas de ADMIN
    boolean esRutaAdmin = RUTAS_ADMIN.stream().anyMatch(path::startsWith);
    if (esRutaAdmin && rol != RolUsuario.ADMIN) {
      // Un TECNICO intenta acceder a una ruta de ADMIN
      res.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado. Se requiere rol de Administrador.");
      return;
    }

    // 3.2: Proteger rutas de TÉCNICO
    // (Asumimos que el ADMIN SÍ puede ver las rutas de TÉCNICO)
    boolean esRutaTecnico = RUTAS_TECNICO.stream().anyMatch(path::startsWith);
    if (esRutaTecnico && (rol != RolUsuario.TECNICO && rol != RolUsuario.ADMIN)) {
      // Un usuario con rol desconocido (si lo hubiera) intenta acceder
      res.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado.");
      return;
    }

    // Si pasa todas las validaciones: continuar con la petición
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    // Método de limpieza
  }
}