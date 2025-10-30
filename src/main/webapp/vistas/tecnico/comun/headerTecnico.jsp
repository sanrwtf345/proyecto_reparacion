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

    <style>
        body {
            background-color: #f8f9fa;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }
        .main-content {
            flex: 1;
        }

        /* Estilos de menuTecnico.jsp */
        .card-menu { border: none; border-radius: 15px; transition: transform 0.3s; height: 100%; display: flex; flex-direction: column; }
        .card-menu:hover { transform: translateY(-5px); box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); }
        .card-body-custom { display: flex; flex-direction: column; flex-grow: 1; }
        .mt-auto { margin-top: auto !important; }

        /* Estilos de listaReparaciones.jsp */
        .table-responsive-custom { max-width: 100%; overflow-x: auto; }
        .back-button {
            transition: all 0.3s;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }

        /* Estilos de listaClientes.jsp */
        .table th, .table td { vertical-align: middle; }
        .sr-only {
            position: absolute;
            width: 1px;
            height: 1px;
            padding: 0;
            margin: -1px;
            overflow: hidden;
            clip: rect(0, 0, 0, 0);
            white-space: nowrap;
            border-width: 0;
        }

        /* --- NUEVOS ESTILOS DE formularioEquipo.jsp --- */
        .card-shadow { box-shadow: 0 0 30px rgba(0, 0, 0, 0.1); }
        .card-header-custom {
            background-color: #198754; /* Color success de Bootstrap */
            border-radius: 10px 10px 0 0 !important;
        }
        .cliente-info {
            background-color: #e7f1ff;
            padding: 15px;
            border-radius: 8px;
            border-left: 5px solid #007bff;
        }
    </style>
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