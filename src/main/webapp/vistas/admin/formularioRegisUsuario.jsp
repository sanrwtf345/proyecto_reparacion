<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%-- Título dinámico para la pestaña del navegador --%>
    <c:choose>
        <c:when test="${not empty requestScope.usuario and requestScope.usuario.idUsuario > 0}">
            <title>Editar Usuario N° ${usuario.idUsuario}</title>
        </c:when>
        <c:otherwise>
            <title>Registro de Nuevo Usuario</title>
        </c:otherwise>
    </c:choose>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <div class="card shadow-lg mx-auto" style="max-width: 600px;">
            <div class="card-header bg-danger text-white">
                <%-- **TÍTULO DINÁMICO en la Tarjeta** --%>
                <c:choose>
                    <c:when test="${not empty requestScope.usuario and requestScope.usuario.idUsuario > 0}">
                        <h3 class="mb-0">Editar Usuario N° ${usuario.idUsuario}</h3>
                    </c:when>
                    <c:otherwise>
                        <h3 class="mb-0">Registrar Nuevo Usuario</h3>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="card-body">

                <%-- Mensaje de error --%>
                <c:if test="${not empty requestScope.error}">
                    <div class="alert alert-danger">${requestScope.error}</div>
                    <c:remove var="error" scope="request"/>
                </c:if>

                <form action="<%= request.getContextPath() %>/UsuariosController" method="POST">

                    <input type="hidden" name="action" value="guardar">

                    <%-- **PUNTO CLAVE 1: Campo Oculto para ID (Solo en Edición)** --%>
                    <c:if test="${not empty requestScope.usuario and requestScope.usuario.idUsuario > 0}">
                        <input type="hidden" name="idUsuario" value="${usuario.idUsuario}">
                    </c:if>

                    <div class="mb-3">
                        <label for="nombreUsuario" class="form-label">Nombre de Usuario (Login)</label>
                        <%-- **PUNTO CLAVE 2: Valor Pre-llenado** --%>
                        <input type="text" class="form-control" id="nombreUsuario" name="nombreUsuario" required
                               value="${usuario.nombreUsuario}">
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="nombre" class="form-label">Nombre</label>
                            <input type="text" class="form-control" id="nombre" name="nombre" required
                                   value="${usuario.nombre}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="apellido" class="form-label">Apellido</label>
                            <input type="text" class="form-control" id="apellido" name="apellido" required
                                   value="${usuario.apellido}">
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="password" class="form-label">Contraseña</label>
                            <input type="password" class="form-control" id="password" name="password"
                                   <%-- **Contraseña: Requerida solo si es nuevo registro** --%>
                                   <c:if test="${empty requestScope.usuario}">
                                       required
                                   </c:if>
                                   placeholder="${not empty usuario ? 'Dejar vacío para NO cambiar' : 'Requerida para registrar'}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="rol" class="form-label">Rol</label>
                            <select class="form-select" id="rol" name="rol" required>
                                <option value="" disabled
                                    <c:if test="${empty usuario.rol}">selected</c:if>
                                    >Seleccione el rol</option>

                                <%-- **PUNTO CLAVE 3: Pre-seleccionar el rol** --%>
                                <option value="ADMIN" ${usuario.rol eq 'ADMIN' ? 'selected' : ''}>Administrador</option>
                                <option value="TECNICO" ${usuario.rol eq 'TECNICO' ? 'selected' : ''}>Técnico</option>


                            </select>
                        </div>
                    </div>

                    <div class="d-grid gap-2 mt-4">
                        <%-- **Texto del Botón Dinámico** --%>
                        <button type="submit" class="btn btn-danger btn-lg">
                            <c:choose>
                                <c:when test="${not empty requestScope.usuario and requestScope.usuario.idUsuario > 0}">
                                    Guardar Cambios
                                </c:when>
                                <c:otherwise>
                                    Registrar Usuario
                                </c:otherwise>
                            </c:choose>
                        </button>
                        <a href="<%= request.getContextPath() %>/UsuariosController?action=listar" class="btn btn-secondary">
                            Cancelar y Volver al Listado
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <script src="