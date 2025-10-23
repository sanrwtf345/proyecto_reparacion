<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Gestión de Usuarios - Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5" role="main">
        <h1 class="mb-4" id="listado-title">Gestión de Usuarios</h1>

        <%-- Contenedor de mensajes de estado con rol ARIA 'alert' para que sean leídos --%>
        <div role="alert" aria-live="assertive">
            <c:if test="${not empty sessionScope.error}">
                <div class="alert alert-danger">${sessionScope.error}</div>
                <c:remove var="error" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.success}">
                <div class="alert alert-success">${sessionScope.success}</div>
                <c:remove var="success" scope="session"/>
            </c:if>
        </div>

        <%-- Tabla de listado de usuarios --%>
        <%-- ARIA: Tabla simple HTML, se añade aria-labelledby para asociar el título principal --%>
        <table class="table table-striped table-bordered" aria-labelledby="listado-title">
            <thead class="table-dark">
                <tr>
                    <th>ID</th>
                    <th>Usuario</th>
                    <th>Nombre Completo</th>
                    <th>Rol</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                <%-- Iteramos sobre la lista de usuarios obtenida del Servlet --%>
                <c:forEach var="user" items="${requestScope.listaUsuarios}">
                    <tr>
                        <td>${user.idUsuario}</td>
                        <td>${user.nombreUsuario}</td>
                        <td>${user.nombre} ${user.apellido}</td>
                        <td>${user.rol}</td>
                        <td>
                            <%-- Solo mostramos el botón de eliminar si NO es la cuenta del admin logueado --%>
                            <c:if test="${user.idUsuario != sessionScope.usuarioLogueado.idUsuario}">
                                <a href="<%= request.getContextPath() %>/UsuariosController?action=eliminar&idUsuario=${user.idUsuario}"
                                   class="btn btn-danger btn-sm"
                                   onclick="return confirm('¿Estás seguro de que deseas eliminar a ${user.nombreUsuario}?');"
                                   aria-label="Eliminar usuario ${user.nombreUsuario}">
                                    Eliminar
                                </a>
                            </c:if>

                            <a href="<%= request.getContextPath() %>/UsuariosController?action=editar&idUsuario=${user.idUsuario}"
                               class="btn btn-info btn-sm text-white"
                               aria-label="Editar usuario ${user.nombreUsuario}">Editar</a>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>

        <a href="<%= request.getContextPath() %>/vistas/admin/menuAdmin.jsp" class="btn btn-secondary" role="button">Volver al Menú</a>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>