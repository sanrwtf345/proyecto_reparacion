<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<%-- 1. Incluimos el HEADER de Técnico --%>
<jsp:include page="/vistas/tecnico/comun/headerTecnico.jsp">
    <jsp:param name="tituloPagina" value="Listado de Órdenes"/>
</jsp:include>


<%-- INICIO DEL CONTENIDO DE LA PÁGINA --%>
<h1 class="mb-4 text-dark"><i class="bi bi-list-check me-2"></i>Gestión de Órdenes de Reparación</h1>

<%-- Bloque de mensajes de éxito/error con ARIA live regions --%>
<div role="region" aria-live="polite">
    <%-- Mensaje de éxito --%>
    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert" aria-label="Alerta de éxito: ${sessionScope.success}">
            <i class="bi bi-check-circle-fill me-2"></i>${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar alerta de éxito"></button>
        </div>
        <c:remove var="success" scope="session"/>
    </c:if>

    <%-- Mensaje de error --%>
    <c:if test="${not empty requestScope.error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert" aria-label="Alerta de error: ${requestScope.error}">
            <i class="bi bi-x-octagon-fill me-2"></i>${requestScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar alerta de error"></button>
        </div>
    </c:if>
</div>

<a href="${pageContext.request.contextPath}/vistas/tecnico/menuTecnico.jsp"
   class="btn btn-secondary back-button mb-3"
   role="button"
   aria-label="Volver al Menú Principal del Técnico">
    <i class="bi bi-arrow-left-circle me-2"></i>Volver al Menú
</a>

<div class="table-responsive-custom">
    <table class="table table-striped table-hover align-middle shadow-sm"
           role="table"
           aria-label="Listado de Órdenes de Reparación activas">

        <thead class="table-dark">
            <tr>
                <th id="th-id">ID Orden</th>
                <th id="th-cliente">Cliente</th>
                <th id="th-equipo">Equipo</th>
                <th id="th-falla">Falla Reportada</th>
                <th id="th-estado"><i class="bi bi-bar-chart-fill" aria-hidden="true"></i> Estado</th>
                <th id="th-fecha">Fecha Recepción</th>
                <th id="th-tecnico">Técnico</th>
                <th id="th-acciones">Acciones</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="orden" items="${requestScope.listaReparaciones}">
                <c:set var="estado" value="${orden.estado}" />
                <tr>
                    <td class="fw-bold text-primary" headers="th-id">#${orden.idReparacion}</td>
                    <td headers="th-cliente">${orden.equipo.cliente.nombre} ${orden.equipo.cliente.apellido}</td>
                    <td headers="th-equipo">${orden.equipo.tipoEquipo} (${orden.equipo.marca})</td>
                    <td headers="th-falla">${orden.equipo.problemaReportado}</td>
                    <td headers="th-estado">
                        <%-- Lógica para asignar un color de badge según el estado --%>
                        <c:choose>
                            <c:when test="${estado eq 'RECIBIDO' or estado eq 'RECEPCIONADO'}">
                                <span class="badge bg-danger text-uppercase" role="status">Pendiente</span>
                            </c:when>
                            <c:when test="${estado eq 'DIAGNOSTICO'}">
                                <span class="badge bg-warning text-dark text-uppercase" role="status">Diagnóstico</span>
                            </c:when>
                            <c:when test="${estado eq 'PENDIENTE_PRESUPUESTO'}">
                                <span class="badge bg-info text-dark text-uppercase" role="status">Presupuesto</span>
                            </c:when>
                            <c:when test="${estado eq 'EN_REPARACION'}">
                                <span class="badge bg-primary text-uppercase" role="status">En Reparación</span>
                            </c:when>
                            <c:when test="${estado eq 'TERMINADO'}">
                                <span class="badge bg-success text-uppercase" role="status">Terminado</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-secondary text-uppercase" role="status">${estado}</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td headers="th-fecha">${orden.fechaRecepcion}</td>
                    <td headers="th-tecnico">${orden.usuario.nombre}</td>
                    <td headers="th-acciones">
                        <a href="${pageContext.request.contextPath}/ReparacionController?action=verDetalle&id=${orden.idReparacion}"
                           class="btn btn-sm btn-info text-white"
                           role="button"
                           aria-label="Ver Detalle de la Orden #${orden.idReparacion}">
                            <i class="bi bi-eye-fill" aria-hidden="true"></i> Detalle
                        </a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty requestScope.listaReparaciones}">
                <tr>
                    <td colspan="8" class="text-center p-4 fs-5 text-muted">
                        <i class="bi bi-inbox me-2" aria-hidden="true"></i> ¡Genial! No hay órdenes de reparación activas.
                    </td>
                </tr>
            </c:if>
        </tbody>
    </table>
</div>
<%-- FIN DEL CONTENIDO DE LA PÁGINA --%>


<%-- 2. Incluimos el FOOTER de Técnico --%>
<jsp:include page="/vistas/tecnico/comun/footerTecnico.jsp" />