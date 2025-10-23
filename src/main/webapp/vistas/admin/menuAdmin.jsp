<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>
<c:set var="usuario" value="${sessionScope.usuarioLogueado}" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel Administrador</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Se incluye el CSS de Bootstrap Icons para los íconos -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>
        body { background-color: #f8f9fa; }
        .card-menu { border: none; border-radius: 15px; transition: transform 0.3s; }
        .card-menu:hover { transform: translateY(-5px); box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); }
    </style>
</head>
<body>

    <%-- ARIA: role="navigation" para la barra de navegación --%>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary" role="navigation" aria-label="Menú Principal de Administración">
        <div class="container-fluid">
            <a class="navbar-brand" href="#" aria-label="Inicio del Panel de Administración">Taller System | Administrador</a>

            <%-- ARIA: role="status" para información dinámica pero no crítica --%>
            <span class="navbar-text ms-auto text-light me-3" role="status">
                Bienvenido, ${usuario.nombre} ${usuario.apellido} (ADMIN)
            </span>

            <a href="<%= request.getContextPath() %>/LogoutServlet" class="btn btn-outline-light" role="button">
                Cerrar Sesión
            </a>
        </div>
    </nav>

    <%-- ARIA: role="main" para el contenido principal del panel --%>
    <div class="container mt-5" role="main">
        <h1 class="mb-4 text-center">Panel de Control General</h1>

        <%-- ARIA: role="region" para agrupar contenido relacionado (Gestión de Usuarios) --%>
        <h2 class="border-bottom pb-2 mb-4 text-secondary" id="gestion-usuarios-heading">Gestión de Usuarios</h2>
        <div class="row row-cols-1 row-cols-md-2 g-4 mb-5" role="region" aria-labelledby="gestion-usuarios-heading">

            <div class="col">
                <div class="card card-menu h-100 shadow-sm border-danger">
                    <div class="card-body">
                        <h5 class="card-title text-danger"><i class="bi bi-person-plus-fill me-2" aria-hidden="true"></i>Registrar Técnico</h5>
                        <p class="card-text">Crear nuevas cuentas para técnicos y administradores con credenciales y roles.</p>
                        <%-- ARIA: role="button" ya se aplica en <a> con clase btn --%>
                        <a href="<%= request.getContextPath() %>/UsuariosController?action=formularioRegisUsuario"
                           class="btn btn-danger w-100"
                           aria-describedby="reg-tec-desc"
                           role="button">
                            Alta de Usuario
                        </a>
                        <p id="reg-tec-desc" class="visually-hidden">Enlace para registrar un nuevo técnico o administrador.</p>
                    </div>
                </div>
            </div>

            <div class="col">
                <div class="card card-menu h-100 shadow-sm border-info">
                    <div class="card-body">
                        <h5 class="card-title text-info"><i class="bi bi-list-ul me-2" aria-hidden="true"></i>Listar y Eliminar Usuarios</h5>
                        <p class="card-text">Ver todos los usuarios registrados y gestionar la eliminación de cuentas.</p>
                        <a href="<%= request.getContextPath() %>/UsuariosController?action=listar"
                           class="btn btn-info w-100"
                           aria-describedby="list-elim-desc"
                           role="button">
                            Ver Listado
                        </a>
                        <p id="list-elim-desc" class="visually-hidden">Enlace para acceder al listado de usuarios, donde se pueden ver, editar y eliminar cuentas.</p>
                    </div>
                </div>
            </div>

        </div>

        <%-- ARIA: role="region" para agrupar contenido relacionado (Tareas Operativas) --%>
        <h2 class="border-bottom pb-2 mb-4 text-secondary" id="tareas-operativas-heading">Tareas Operativas</h2>
        <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4" role="region" aria-labelledby="tareas-operativas-heading">

            <div class="col">
                <div class="card card-menu h-100 shadow-sm border-primary">
                    <div class="card-body">
                        <h5 class="card-title text-primary">Nueva Orden</h5>
                        <p class="card-text">Registrar cliente, equipo y generar la orden de servicio.</p>
                        <a href="${pageContext.request.contextPath}/ClienteEquipoController"
                           class="btn btn-primary w-100"
                           aria-describedby="nueva-orden-desc"
                           role="button">
                            Registrar Equipo
                        </a>
                        <p id="nueva-orden-desc" class="visually-hidden">Enlace para iniciar el registro de un nuevo equipo y una nueva orden de servicio.</p>
                    </div>
                </div>
            </div>

            <div class="col">
                <div class="card card-menu h-100 shadow-sm border-warning">
                    <div class="card-body">
                        <h5 class="card-title text-warning">Órdenes Pendientes</h5>
                        <p class="card-text">Ver listado de órdenes en espera de diagnóstico o presupuesto.</p>
                        <a href="${pageContext.request.contextPath}/ReparacionController?action=listar"
                           class="btn btn-warning w-100 text-dark"
                           aria-describedby="ordenes-pendientes-desc"
                           role="button">
                            Ver Pendientes
                        </a>
                         <p id="ordenes-pendientes-desc" class="visually-hidden">Enlace para ver el listado de todas las órdenes de servicio pendientes.</p>
                    </div>
                </div>
            </div>

        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>