package proyecto.interfaces;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // Cierra el pool al apagar Tomcat para evitar fugas de memoria
    AdminConexion.INSTANCE.cerrarPool();
  }
}