<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<%-- HEADER DINÁMICO --%>
<c:choose>
    <c:when test="${sessionScope.usuarioLogueado.rol eq 'ADMIN'}">
        <jsp:include page="/vistas/admin/comun/headerAdmin.jsp"><jsp:param name="tituloPagina" value="Historial de Equipo"/></jsp:include>
    </c:when>
    <c:otherwise>
        <jsp:include page="/vistas/tecnico/comun/headerTecnico.jsp"><jsp:param name="tituloPagina" value="Historial de Equipo"/></jsp:include>
    </c:otherwise>
</c:choose>

<div class="container mt-4">

    <%-- TARJETA DE RESUMEN DEL EQUIPO --%>
    <div class="card mb-4 border-primary">
        <div class="card-body">
            <h4 class="card-title text-primary"><i class="bi bi-clock-history me-2"></i>Historial de Reparaciones</h4>
            <h6 class="card-subtitle mb-2 text-muted">
                Equipo: <strong>${equipo.tipoEquipo} ${equipo.marca} ${equipo.modelo}</strong>
                <c:if test="${not empty equipo.numeroSerie}">(S/N: ${equipo.numeroSerie})</c:if>
            </h6>
            <p class="card-text mb-0">Cliente: ${equipo.cliente.nombre} ${equipo.cliente.apellido}</p>
        </div>
    </div>

    <div class="d-flex justify-content-between mb-3">
        <%-- Botón para agregar una nueva orden directamente desde aquí --%>
        <a href="${pageContext.request.contextPath}/ReparacionController?action=nueva&idEquipo=${equipo.idEquipo}"
           class="btn btn-success">
            <i class="bi bi-plus-lg me-1"></i> Nueva Orden
        </a>

        <%-- Botón Volver a la lista de equipos de ESTE cliente --%>
        <a href="${pageContext.request.contextPath}/EquipoController?action=listarPorCliente&idCliente=${equipo.cliente.idCliente}"
           class="btn btn-secondary">
            <i class="bi bi-arrow-left me-1"></i> Volver a Equipos
        </a>
    </div>

    <div class="table-responsive bg-white p-3 rounded shadow-sm">
        <table class="table table-hover align-middle">
            <thead class="table-light">
                <tr>
                    <th>ID Orden</th>
                    <th>Fecha Ingreso</th>
                    <th>Estado</th>
                    <th>Diagnóstico / Trabajo</th>
                    <th>Técnico</th>
                    <th>Costo Total</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="orden" items="${requestScope.historial}">
                    <%-- Convertimos el Enum a String para el c:choose si es necesario, o comparamos directemente --%>
                    <c:set var="estado" value="${orden.estado}" />
                    <tr>
                        <td class="fw-bold">#${orden.idReparacion}</td>
                        <%-- Usamos toLocalDate() porque es LocalDateTime --%>
                        <td>${orden.fechaCreacion.toLocalDate()}</td>
                        <td>
                            <c:choose>
                                <c:when test="${estado == 'PENDIENTE'}"><span class="badge bg-danger">Pendiente</span></c:when>
                                <c:when test="${estado == 'EN_PROCESO'}"><span class="badge bg-primary">En Proceso</span></c:when>
                                <c:when test="${estado == 'FINALIZADO'}"><span class="badge bg-info text-dark">Finalizado</span></c:when>
                                <c:when test="${estado == 'TERMINADO'}"><span class="badge bg-success">Entregado</span></c:when>
                                <c:otherwise><span class="badge bg-secondary">${estado}</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-truncate" style="max-width: 200px;" title="${orden.diagnosticoFinal}">
                            ${orden.diagnosticoFinal}
                        </td>
                        <td>${orden.usuario.nombre}</td>
                        <td class="fw-bold">$ ${orden.presupuestoTotal}</td>
                        <td>
                            <a href="${pageContext.request.contextPath}/ReparacionController?action=verDetalle&id=${orden.idReparacion}"
                               class="btn btn-sm btn-outline-primary" title="Ver Detalle">
                                <i class="bi bi-eye"></i>
                            </a>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty requestScope.historial}">
                    <tr>
                        <td colspan="7" class="text-center p-4 text-muted">
                            Este equipo no tiene reparaciones registradas.
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/vistas/tecnico/comun/footerTecnico.jsp" />