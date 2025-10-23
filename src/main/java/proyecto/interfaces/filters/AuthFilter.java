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

  // Rutas que NO requieren iniciar sesión
  private static final List<String> PUBLIC_PATHS = Arrays.asList(
      "/login.jsp",
      "/LoginServlet",
      "/LogoutServlet",
      "/css/",            // Permite acceder a la carpeta de CSS
      "/js/",             // Permite acceder a la carpeta de JavaScript
      "/images/"          // Permite acceder a imágenes/recursos estáticos
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

    // No crear la sesión si no existe
    HttpSession session = req.getSession(false);

    // 1. Obtener la ruta solicitada (ej: /vistas/tecnico/menuTecnico.jsp)
    String path = req.getRequestURI().substring(req.getContextPath().length());

    // 2. Comprobar si la ruta es pública o un recurso estático (CSS, JS, etc.)
    boolean isPublicPath = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    boolean isStaticResource = path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png") || path.endsWith(".jpg");

    // 3. Obtener el usuario de la sesión
    Usuarios usuarioLogueado = (session != null) ? (Usuarios) session.getAttribute("usuarioLogueado") : null;

    // =========================================================
    // LÓGICA DE CONTROL DE ACCESO
    // =========================================================

    // Permitir el paso si es la raíz, una página pública o un recurso estático
    if (isPublicPath || isStaticResource || path.equals("/")) {
      chain.doFilter(request, response);
      return;
    }

    // CASO 1: Bloquear si no está logueado
    if (usuarioLogueado == null) {
      // Usuario NO logueado: Redirigir al login
      res.sendRedirect(req.getContextPath() + "/login.jsp");
      return;
    }

    // CASO 2: Autorización por Rol
    RolUsuario rol = usuarioLogueado.getRol();

    // Restricción: Solo ADMIN puede acceder a rutas que comienzan con /vistas/admin/
    if (path.startsWith("/vistas/admin/") && rol != RolUsuario.ADMIN) {
      // Si un TECNICO intenta acceder a una vista de ADMIN
      res.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado. Se requiere rol de Administrador para esta función.");
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