<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="usuario" value="${sessionScope.usuarioLogueado}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel Administrador</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f8f9fa; }
        .card-menu { border: none; border-radius: 15px; transition: transform 0.3s; }
        .card-menu:hover { transform: translateY(-5px); box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); }
    </style>
</head>
<body>

    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">Taller System | Administrador</a>
            <span class="navbar-text ms-auto text-light me-3">
                Bienvenido, ${usuario.nombre} ${usuario.apellido} (ADMIN)
            </span>

            <a href="<%= request.getContextPath() %>/LogoutServlet" class="btn btn-outline-light">
                Cerrar Sesión
            </a>
        </div>
    </nav>

    <div class="container mt-5">
        <h1 class="mb-4 text-center">Panel de Control General</h1>

        <h2 class="border-bottom pb-2 mb-4 text-secondary">Gestión de Usuarios</h2>
        <div class="row row-cols-1 row-cols-md-2 g-4 mb-5">

            <div class="col">
                <div class="card card-menu h-100 shadow-sm border-danger">
                    <div class="card-body">
                        <h5 class="card-title text-danger"><i class="bi bi-person-plus-fill me-2"></i>Registrar Técnico</h5>
                        <p class="card-text">Crear nuevas cuentas para técnicos y administradores con credenciales y roles.</p>
                        <a href="<%= request.getContextPath() %>/UsuariosController?action=formularioRegisUsuario" class="btn btn-danger w-100">
                            Alta de Usuario
                        </a>
                    </div>
                </div>
            </div>

            <div class="col">
                <div class="card card-menu h-100 shadow-sm border-info">
                    <div class="card-body">
                        <h5 class="card-title text-info"><i class="bi bi-list-ul me-2"></i>Listar y Eliminar Usuarios</h5>
                        <p class="card-text">Ver todos los usuarios registrados y gestionar la eliminación de cuentas.</p>
                        <a href="<%= request.getContextPath() %>/UsuariosController?action=listar" class="btn btn-info w-100">
                            Ver Listado
                        </a>
                    </div>
                </div>
            </div>

        </div>

        <h2 class="border-bottom pb-2 mb-4 text-secondary">Tareas Operativas</h2>
        <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">

            <div class="col">
                <div class="card card-menu h-100 shadow-sm border-primary">
                    <div class="card-body">
                        <h5 class="card-title text-primary">Nueva Orden</h5>
                        <p class="card-text">Registrar cliente, equipo y generar la orden de servicio.</p>
                        <a href="${pageContext.request.contextPath}/ClienteEquipoController" class="btn btn-primary w-100">
                            Registrar Equipo
                        </a>
                    </div>
                </div>
            </div>

            <div class="col">
                <div class="card card-menu h-100 shadow-sm border-warning">
                    <div class="card-body">
                        <h5 class="card-title text-warning">Órdenes Pendientes</h5>
                        <p class="card-text">Ver listado de órdenes en espera de diagnóstico o presupuesto.</p>
                        <a href="${pageContext.request.contextPath}/ReparacionController?action=listar" class="btn btn-warning w-100 text-dark">
                            Ver Pendientes
                        </a>
                    </div>
                </div>
            </div>

        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>