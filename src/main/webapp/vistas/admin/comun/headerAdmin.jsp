<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<%--
  Obtenemos el usuario de la sesión.
  Esto debe estar en el header porque la barra de navegación lo usa.
--%>
<c:set var="usuario" value="${sessionScope.usuarioLogueado}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <%--
      TÍTULO DINÁMICO:
      Recibimos un parámetro "tituloPagina". Si no existe, ponemos un título por defecto.
    --%>
    <title>${not empty param.tituloPagina ? param.tituloPagina : 'Panel Admin'} - Taller System</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

    <%-- Bloque <style> eliminado y reemplazado por el enlace al CSS externo --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">

</head>
<body>

    <%-- ARIA: role="navigation" para la barra de navegación --%>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary" role="navigation" aria-label="Menú Principal de Administración">
        <div class="container-fluid">
            <%-- Enlace al menú principal --%>
            <a class="navbar-brand" href="${pageContext.request.contextPath}/vistas/admin/menuAdmin.jsp" aria-label="Inicio del Panel de Administración">
                Taller System | Administrador
            </a>

            <%-- ARIA: role="status" para información dinámica pero no crítica --%>
            <span class="navbar-text ms-auto text-light me-3" role="status">
                Bienvenido, ${usuario.nombre} ${usuario.apellido} (ADMIN)
            </span>

            <a href="<%= request.getContextPath() %>/LogoutServlet" class="btn btn-outline-light" role="button">
                Cerrar Sesión
            </a>
        </div>
    </nav>

<%--
  Aquí termina el header.
  El <body> se abre aquí, pero se cierra en el footer.
  Iniciamos el contenedor del contenido principal.
--%>
<div class="container mt-5 main-content" role="main">