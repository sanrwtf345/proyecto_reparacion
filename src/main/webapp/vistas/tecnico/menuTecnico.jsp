<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="usuario" value="${sessionScope.usuarioLogueado}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Menú Técnico - Gestión de Tareas</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>
        body { background-color: #f8f9fa; }
        .card-menu { border: none; border-radius: 15px; transition: transform 0.3s; }
        .card-menu:hover { transform: translateY(-5px); box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); }
    </style>
</head>
<body>

    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">Taller System | Técnico</a>
            <span class="navbar-text ms-auto text-light me-3">
                Bienvenido, ${usuario.nombre} ${usuario.apellido}
            </span>
            <a href="<%= request.getContextPath() %>/LogoutServlet" class="btn btn-outline-danger">
                Cerrar Sesión
            </a>
        </div>
    </nav>

    <div class="container mt-5">
        <h1 class="mb-4 text-center">Panel de Tareas Operativas</h1>

        <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">

            <div class="col">
                <div class="card card-menu h-100 shadow-sm border-primary">
                    <div class="card-body">
                        <h5 class="card-title text-primary"><i class="bi bi-file-earmark-plus-fill me-2"></i>Nueva Orden</h5>
                        <p class="card-text">Registrar un nuevo cliente, su equipo y generar la orden de servicio inicial.</p>

                        <a href="<%= request.getContextPath() %>/ClienteEquipoController" class="btn btn-primary w-100">
                            Iniciar Registro de Cliente
                        </a>
                    </div>
                </div>
            </div>

            <div class="col">
                <div class="card card-menu h-100 shadow-sm border-warning">
                    <div class="card-body">
                        <h5 class="card-title text-warning"><i class="bi bi-list-task me-2"></i>Órdenes Pendientes</h5>
                        <p class="card-text">Ver listado de todas las órdenes de reparación en espera de diagnóstico o presupuesto.</p>
                        <a href="${pageContext.request.contextPath}/ReparacionController?action=listar" class="btn btn-warning w-100 text-dark">
                            Ver Pendientes
                        </a>
                    </div>
                </div>
            </div>

            <%-- Puedes añadir aquí un tercer menú para "Consultar Clientes Existentes" si lo necesitas --%>

        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <%-- Importante: añadí el link de Bootstrap Icons que faltaba --%>
</body>
</html>