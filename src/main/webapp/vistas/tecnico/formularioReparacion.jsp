<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<%-- HEADER DINÁMICO --%>
<c:choose>
    <c:when test="${sessionScope.usuarioLogueado.rol eq 'ADMIN'}">
        <jsp:include page="/vistas/admin/comun/headerAdmin.jsp"><jsp:param name="tituloPagina" value="${titulo}"/></jsp:include>
    </c:when>
    <c:otherwise>
        <jsp:include page="/vistas/tecnico/comun/headerTecnico.jsp"><jsp:param name="tituloPagina" value="${titulo}"/></jsp:include>
    </c:otherwise>
</c:choose>

<div class="card card-shadow mx-auto" style="max-width: 900px; border-radius: 10px;">

    <%-- Cabecera con color info --%>
    <div class="card-header bg-info text-white text-center">
        <h3 class="mb-0 fw-bold"><i class="bi bi-tools me-2"></i>${titulo}</h3>
    </div>

    <div class="card-body p-4">

        <%-- RESUMEN DEL EQUIPO (Solo Lectura) --%>
        <div class="alert alert-secondary mb-4">
            <h5 class="alert-heading"><i class="bi bi-laptop me-2"></i>Equipo en Reparación</h5>
            <div class="row">
                <div class="col-md-4">
                    <strong>Equipo:</strong> ${reparacion.equipo.tipoEquipo} ${reparacion.equipo.marca}
                </div>
                <div class="col-md-4">
                    <strong>Modelo:</strong> ${reparacion.equipo.modelo}
                </div>
                <div class="col-md-4">
                    <strong>S/N:</strong> ${reparacion.equipo.numeroSerie}
                </div>
            </div>
            <hr>
            <p class="mb-0"><strong>Falla Reportada por Cliente:</strong> ${reparacion.equipo.problemaReportado}</p>
        </div>

        <form action="<%= request.getContextPath() %>/ReparacionController" method="POST">

            <%-- Definir acción: Si hay ID > 0 es actualizar, si no es guardar --%>
            <input type="hidden" name="action" value="${reparacion.idReparacion > 0 ? 'actualizar' : 'guardar'}">

            <%-- IDs necesarios --%>
            <input type="hidden" name="idReparacion" value="${reparacion.idReparacion}">
            <input type="hidden" name="idEquipo" value="${reparacion.equipo.idEquipo}">

            <div class="row g-3">

                <%-- ESTADO Y FECHAS --%>
                <div class="col-md-4">
                    <label for="estado" class="form-label fw-bold">Estado Actual</label>
                    <select class="form-select" id="estado" name="estado" required>
                        <c:forEach var="est" items="${listaEstados}">
                            <option value="${est}" ${reparacion.estado == est ? 'selected' : ''}>${est}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-4">
                    <label for="fechaDiagnostico" class="form-label">Fecha Diagnóstico</label>
                    <input type="date" class="form-control" id="fechaDiagnostico" name="fechaDiagnostico"
                           value="${reparacion.fechaDiagnostico}">
                </div>
                <div class="col-md-4">
                    <label for="fechaEntrega" class="form-label">Fecha Entrega Estimada</label>
                    <input type="date" class="form-control" id="fechaEntrega" name="fechaEntrega"
                           value="${reparacion.fechaEntregaEstimada}">
                </div>

                <%-- DIAGNÓSTICO TÉCNICO --%>
                <div class="col-12 mt-4">
                    <label for="diagnosticoFinal" class="form-label fw-bold">Diagnóstico Técnico y Trabajo Realizado</label>
                    <textarea class="form-control" id="diagnosticoFinal" name="diagnosticoFinal" rows="5" required>${reparacion.diagnosticoFinal}</textarea>
                    <div class="form-text">Describa el problema real encontrado y la solución aplicada.</div>
                </div>

                <%-- COSTOS --%>
                <div class="col-12 mt-4">
                    <h5 class="text-secondary border-bottom pb-2"><i class="bi bi-cash-coin me-2"></i>Presupuesto</h5>
                </div>

                <div class="col-md-4">
                    <label for="costoRepuestos" class="form-label">Costo Repuestos ($)</label>
                    <input type="number" step="0.01" class="form-control" id="costoRepuestos" name="costoRepuestos"
                           value="${reparacion.costoRepuestos}" placeholder="0.00">
                </div>
                <div class="col-md-4">
                    <label for="costoManoObra" class="form-label">Costo Mano de Obra ($)</label>
                    <input type="number" step="0.01" class="form-control" id="costoManoObra" name="costoManoObra"
                           value="${reparacion.costoManoObra}" placeholder="0.00">
                </div>
                <div class="col-md-4">
                    <label class="form-label text-muted">Total (Calculado al guardar)</label>
                    <input type="text" class="form-control bg-light" value="$ ${reparacion.presupuestoTotal}" readonly>
                </div>
            </div>

            <div class="d-grid gap-2 d-md-flex justify-content-md-end mt-5">

                <c:choose>
                    <c:when test="${sessionScope.usuarioLogueado.rol eq 'ADMIN'}">
                        <a href="<%= request.getContextPath() %>/ReparacionController?action=listar" class="btn btn-secondary me-md-2">Cancelar</a>
                    </c:when>
                    <c:otherwise>
                        <a href="<%= request.getContextPath() %>/ReparacionController?action=listar" class="btn btn-secondary me-md-2">Cancelar</a>
                    </c:otherwise>
                </c:choose>

                <button type="submit" class="btn btn-primary btn-lg px-5">
                    <i class="bi bi-save me-2"></i>Guardar Orden
                </button>
            </div>

        </form>
    </div>
</div>

<jsp:include page="/vistas/tecnico/comun/footerTecnico.jsp" />