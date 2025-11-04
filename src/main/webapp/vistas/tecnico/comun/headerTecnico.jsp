<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>
<c:set var="usuario" value="${sessionScope.usuarioLogueado}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>${not empty param.tituloPagina ? param.tituloPagina : 'Panel Técnico'} - Taller System</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

    <%--
      Bloque <style> eliminado.
      Los estilos ahora se cargan desde el archivo CSS externo.
    --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tecnico.css">

</head>
<body>

    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/vistas/tecnico/menuTecnico.jsp">Taller System | Técnico</a>
            <span class="navbar-text ms-auto text-light me-3">
                Bienvenido, ${usuario.nombre} ${usuario.apellido}
            </span>
            <a href="${pageContext.request.contextPath}/LogoutServlet" class="btn btn-outline-danger">
                <i class="bi bi-box-arrow-right me-1"></i>Cerrar Sesión
            </a>
        </div>
    </nav>

    <div class="container mt-5 main-content" role="main">