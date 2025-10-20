<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Listado de Órdenes de Reparación</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <h1 class="mb-4">Órdenes de Reparación Activas</h1>

        <%-- Mensaje de éxito (viene del ClienteServlet) --%>
        <c:if test="${not empty sessionScope.success}">
            <div class="alert alert-success">${sessionScope.success}</div>
            <c:remove var="success" scope="session"/>
        </c:if>

        <%-- Mensaje de error --%>
        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger">${requestScope.error}</div>
        </c:if>

        <a href="${pageContext.request.contextPath}/vistas/tecnico/menuTecnico.jsp" class="btn btn-secondary mb-3">
            Volver al Menú
        </a>

        <table class="table table-striped table-hover">
            <thead class="table-dark">
                <tr>
                    <th>ID Orden</th>
                    <th>Cliente</th>
                    <th>Tipo Equipo</th>
                    <th>Estado</th>
                    <th>Fecha Recepción</th>
                    <th>Técnico Asignado</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="orden" items="${requestScope.listaReparaciones}">
                    <tr>
                        <td>${orden.idReparacion}</td>
                        <td>${orden.equipo.cliente.nombre} ${orden.equipo.cliente.apellido}</td>
                        <td>${orden.equipo.tipoEquipo}</td>
                        <td><span class="badge bg-primary">${orden.estado}</span></td>
                        <td>${orden.fechaRecepcion}</td>
                        <td>${orden.usuario.nombre}</td>
                        <td>
                            <a href="#" class="btn btn-sm btn-info text-white">Ver Detalle</a>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty requestScope.listaReparaciones}">
                    <tr>
                        <td colspan="7" class="text-center">No hay órdenes de reparación activas.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>

    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>