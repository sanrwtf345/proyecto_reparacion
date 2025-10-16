<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %> <%-- Aseguramos que el EL funcione --%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Gestión de Usuarios - Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <h1 class="mb-4">Gestión de Usuarios</h1>

        <%-- Muestra mensajes de éxito/error de la sesión --%>
        <c:if test="${not empty sessionScope.error}">
            <div class="alert alert-danger">${sessionScope.error}</div>
            <c:remove var="error" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.success}">
            <div class="alert alert-success">${sessionScope.success}</div>
            <c:remove var="success" scope="session"/>
        </c:if>

        <table class="table table-striped table-bordered">
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
                                <a href="<%= request.getContextPath() %>/UsuariosController?action=eliminar&id=${user.idUsuario}"
                                   class="btn btn-danger btn-sm"
                                   onclick="return confirm('¿Estás seguro de que deseas eliminar a ${user.nombreUsuario}?');">
                                    Eliminar
                                </a>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>


        <a href="<%= request.getContextPath() %>/vistas/admin/menuAdmin.jsp" class="btn btn-secondary">Volver al Menú</a>
    </div>
</body>
</html>