# Usa la imagen oficial de Tomcat 10 (compatible con Jakarta EE 9/10)
FROM tomcat:10.1-jdk17

# Limpia las aplicaciones por defecto de Tomcat (opcional pero recomendado)
RUN rm -rf /usr/local/tomcat/webapps/*

# Copia el archivo WAR de tu proyecto a la carpeta de despliegue de Tomcat
# Asegúrate de cambiar "SistemaReparaciones-1.0.war" por el nombre real de tu archivo compilado
COPY target/proyecto_reparacion.war /usr/local/tomcat/webapps/ROOT.war

# Expone el puerto 8080
EXPOSE 8080

# Inicia Tomcat
CMD ["catalina.sh", "run"]