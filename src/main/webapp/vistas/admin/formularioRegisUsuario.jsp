<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registro de Nuevo Usuario</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <div class="card shadow-lg mx-auto" style="max-width: 600px;">
            <div class="card-header bg-danger text-white">
                <h3 class="mb-0">Registrar Nuevo Usuario</h3>
            </div>
            <div class="card-body">

                <%-- Mensaje de error (si el servlet envía un error) --%>
                <c:if test="${not empty requestScope.error}">
                    <div class="alert alert-danger">${requestScope.error}</div>
                    <c:remove var="error" scope="request"/>
                </c:if>

                <%-- El formulario enviará los datos al UsuariosController (UsuarioServlet) --%>
                <form action="<%= request.getContextPath() %>/UsuariosController" method="POST">

                    <%-- Campo oculto para indicar qué acción debe ejecutar el doPost --%>
                    <input type="hidden" name="action" value="guardar">

                    <div class="mb-3">
                        <label for="nombreUsuario" class="form-label">Nombre de Usuario (Login)</label>
                        <input type="text" class="form-control" id="nombreUsuario" name="nombreUsuario" required>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="nombre" class="form-label">Nombre</label>
                            <input type="text" class="form-control" id="nombre" name="nombre" required>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="apellido" class="form-label">Apellido</label>
                            <input type="text" class="form-control" id="apellido" name="apellido" required>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="password" class="form-label">Contraseña</label>
                            <input type="password" class="form-control" id="password" name="password" required>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="rol" class="form-label">Rol</label>
                            <select class="form-select" id="rol" name="rol" required>
                                <option value="" disabled selected>Seleccione el rol</option>
                                <option value="ADMIN">Administrador</option>
                                <option value="TECNICO">Técnico</option>
                            </select>
                        </div>
                    </div>

                    <div class="d-grid gap-2 mt-4">
                        <button type="submit" class="btn btn-danger btn-lg">Registrar Usuario</button>
                        <a href="<%= request.getContextPath() %>/vistas/admin/menuAdmin.jsp" class="btn btn-secondary">Cancelar y Volver</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>