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
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>
        body { background-color: #f8f9fa; }
        .table-responsive-custom { max-width: 100%; overflow-x: auto; }
        .back-button {
            transition: all 0.3s;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
    </style>
</head>
<body>
    <div class="container mt-5">
        <h1 class="mb-4 text-dark"><i class="bi bi-list-check me-2"></i>Gestión de Órdenes de Reparación</h1>

        <%-- Mensaje de éxito (viene del ClienteServlet) --%>
        <c:if test="${not empty sessionScope.success}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i>${sessionScope.success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
            <c:remove var="success" scope="session"/>
        </c:if>

        <%-- Mensaje de error --%>
        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-x-octagon-fill me-2"></i>${requestScope.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <a href="${pageContext.request.contextPath}/vistas/tecnico/menuTecnico.jsp" class="btn btn-secondary back-button mb-3">
            <i class="bi bi-arrow-left-circle me-2"></i>Volver al Menú
        </a>

        <div class="table-responsive-custom">
            <table class="table table-striped table-hover align-middle shadow-sm">
                <thead class="table-dark">
                    <tr>
                        <th>ID Orden</th>
                        <th>Cliente</th>
                        <th>Equipo</th>
                        <th>Falla Reportada</th>
                        <th><i class="bi bi-bar-chart-fill"></i> Estado</th>
                        <th>Fecha Recepción</th>
                        <th>Técnico</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="orden" items="${requestScope.listaReparaciones}">
                        <c:set var="estado" value="${orden.estado}" />
                        <tr>
                            <td class="fw-bold text-primary">#${orden.idReparacion}</td>
                            <td>${orden.equipo.cliente.nombre} ${orden.equipo.cliente.apellido}</td>
                            <td>${orden.equipo.tipoEquipo} (${orden.equipo.marca})</td>
                            <td>${orden.equipo.problemaReportado}</td>
                            <td>
                                <%-- Lógica para asignar un color de badge según el estado --%>
                                <c:choose>
                                    <c:when test="${estado eq 'RECIBIDO' or estado eq 'RECEPCIONADO'}">
                                        <span class="badge bg-danger text-uppercase">Pendiente</span>
                                    </c:when>
                                    <c:when test="${estado eq 'DIAGNOSTICO'}">
                                        <span class="badge bg-warning text-dark text-uppercase">Diagnóstico</span>
                                    </c:when>
                                    <c:when test="${estado eq 'PENDIENTE_PRESUPUESTO'}">
                                        <span class="badge bg-info text-dark text-uppercase">Presupuesto</span>
                                    </c:when>
                                    <c:when test="${estado eq 'EN_REPARACION'}">
                                        <span class="badge bg-primary text-uppercase">En Reparación</span>
                                    </c:when>
                                    <c:when test="${estado eq 'TERMINADO'}">
                                        <span class="badge bg-success text-uppercase">Terminado</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-secondary text-uppercase">${estado}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>${orden.fechaRecepcion}</td>
                            <td>${orden.usuario.nombre}</td>
                            <td>
                                <a href="${pageContext.request.contextPath}/ReparacionController?action=verDetalle&id=${orden.idReparacion}" class="btn btn-sm btn-info text-white">
                                    <i class="bi bi-eye-fill"></i> Detalle
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty requestScope.listaReparaciones}">
                        <tr>
                            <td colspan="8" class="text-center p-4 fs-5 text-muted">
                                <i class="bi bi-inbox me-2"></i> ¡Genial! No hay órdenes de reparación activas.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>

    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>