<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<%-- HEADER DINÁMICO: Elige el header según el rol del usuario --%>
<c:choose>
    <c:when test="${sessionScope.usuarioLogueado.rol eq 'ADMIN'}">
        <jsp:include page="/vistas/admin/comun/headerAdmin.jsp"><jsp:param name="tituloPagina" value="Listado de Órdenes (Admin)"/></jsp:include>
    </c:when>
    <c:otherwise>
        <jsp:include page="/vistas/tecnico/comun/headerTecnico.jsp"><jsp:param name="tituloPagina" value="Listado de Órdenes"/></jsp:include>
    </c:otherwise>
</c:choose>


<%-- INICIO DEL CONTENIDO DE LA PÁGINA --%>
<h1 class="mb-4 text-dark text-center"><i class="bi bi-list-check me-2"></i>Gestión de Órdenes de Reparación</h1>

<%-- Bloque de mensajes de éxito/error --%>
<div role="region" aria-live="polite">
    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i>${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
        </div>
        <c:remove var="success" scope="session"/>
    </c:if>

    <c:if test="${not empty requestScope.error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-x-octagon-fill me-2"></i>${requestScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
        </div>
    </c:if>
</div>

<%-- BARRA DE HERRAMIENTAS: Filtro + Botones --%>
<div class="card mb-4 shadow-sm bg-light">
    <div class="card-body py-3">
        <form class="row g-3 align-items-center" action="<%= request.getContextPath() %>/ReparacionController" method="GET">
            <input type="hidden" name="action" value="listar">

            <%-- Filtro por Estado --%>
            <div class="col-auto">
                <label for="filtroEstado" class="col-form-label fw-bold"><i class="bi bi-funnel-fill"></i> Filtrar por Estado:</label>
            </div>
            <div class="col-auto">
                <%-- El onchange envía el formulario automáticamente al seleccionar --%>
                <select class="form-select" id="filtroEstado" name="filtroEstado" onchange="this.form.submit()">
                    <option value="">Todos los Estados</option>

                    <%-- Iteramos sobre los estados que envió el Servlet --%>
                    <c:forEach var="est" items="${listaEstados}">
                        <%-- Usamos c:choose para mostrar un nombre amigable --%>
                        <option value="${est}" ${est.toString() eq requestScope.estadoSeleccionado ? 'selected' : ''}>
                            <c:choose>
                                <c:when test="${est == 'PENDIENTE'}">Pendiente</c:when>
                                <c:when test="${est == 'EN_PROCESO'}">En Proceso</c:when>
                                <c:when test="${est == 'FINALIZADO'}">Finalizado</c:when>
                                <c:when test="${est == 'TERMINADO'}">Entregado</c:when>
                                <c:when test="${est == 'CANCELADO'}">Cancelado</c:when>
                                <c:otherwise>${est}</c:otherwise>
                            </c:choose>
                        </option>
                    </c:forEach>

                </select>
            </div>

            <%-- Botón Limpiar Filtro (Solo aparece si hay filtro activo) --%>
            <c:if test="${not empty requestScope.estadoSeleccionado}">
                <div class="col-auto">
                    <a href="<%= request.getContextPath() %>/ReparacionController?action=listar" class="btn btn-outline-secondary" title="Quitar filtro">
                        <i class="bi bi-x-lg"></i> Limpiar
                    </a>
                </div>
            </c:if>

            <div class="col"></div> <%-- Espaciador --%>

            <%-- Botón Volver Dinámico --%>
            <div class="col-auto">
                <c:choose>
                    <c:when test="${sessionScope.usuarioLogueado.rol eq 'ADMIN'}">
                        <a href="${pageContext.request.contextPath}/vistas/admin/menuAdmin.jsp" class="btn btn-secondary">
                            <i class="bi bi-arrow-left me-1"></i> Volver Admin
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/vistas/tecnico/menuTecnico.jsp" class="btn btn-secondary">
                            <i class="bi bi-arrow-left me-1"></i> Volver Menú
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </form>
    </div>
</div>

<div class="table-responsive-custom">
    <table class="table table-striped table-hover align-middle shadow-sm" role="table">
        <thead class="table-dark">
            <tr>
                <th>ID</th>
                <th>Cliente</th>
                <th>Equipo</th>
                <th>Falla Reportada</th>
                <th>Estado</th>
                <th>Fecha Recepción</th>
                <th>Técnico</th>
                <th>Acciones</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="orden" items="${requestScope.listaReparaciones}">
                <%-- Convertimos el estado (Enum) a String para compararlo fácilmente en el c:choose --%>
                <c:set var="estado" value="${orden.estado}" />
                <tr>
                    <td class="fw-bold text-primary">#${orden.idReparacion}</td>
                    <td>${orden.equipo.cliente.nombre} ${orden.equipo.cliente.apellido}</td>
                    <td>${orden.equipo.tipoEquipo} (${orden.equipo.marca})</td>
                    <td class="text-truncate" style="max-width: 150px;" title="${orden.equipo.problemaReportado}">
                        ${orden.equipo.problemaReportado}
                    </td>
                    <td>
                        <%-- Lógica de Badges actualizada para el Enum --%>
                        <c:choose>
                            <c:when test="${estado eq 'PENDIENTE'}">
                                <span class="badge bg-danger text-uppercase">Pendiente</span>
                            </c:when>
                            <c:when test="${estado eq 'EN_PROCESO'}">
                                <span class="badge bg-primary text-uppercase">En Proceso</span>
                            </c:when>
                            <c:when test="${estado eq 'FINALIZADO'}">
                                <span class="badge bg-info text-dark text-uppercase">Finalizado</span>
                            </c:when>
                            <c:when test="${estado eq 'TERMINADO'}">
                                <span class="badge bg-success text-uppercase">Entregado</span>
                            </c:when>
                            <c:when test="${estado eq 'CANCELADO'}">
                                <span class="badge bg-secondary text-uppercase">Cancelado</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-secondary text-uppercase">${estado}</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <%-- Usamos toLocalDate() porque fechaCreacion es LocalDateTime --%>
                    <td>${orden.fechaCreacion.toLocalDate()}</td>
                    <td>${orden.usuario.nombre}</td>
                    <td>

                        <%-- Botón Editar/Gestionar --%>
                        <a href="${pageContext.request.contextPath}/ReparacionController?action=editar&id=${orden.idReparacion}"
                           class="btn btn-sm btn-info text-white me-1"
                           title="Gestionar Orden">
                            <i class="bi bi-pencil-square"></i> Gestionar
                        </a>

                        <%-- Botón Eliminar --%>
                        <a href="${pageContext.request.contextPath}/ReparacionController?action=eliminar&id=${orden.idReparacion}"
                           class="btn btn-sm btn-danger"
                           onclick="return confirm('¿Está seguro de eliminar esta orden definitivamente?');"
                           title="Eliminar Orden">
                            <i class="bi bi-trash"></i>
                        </a>

                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty requestScope.listaReparaciones}">
                <tr>
                    <td colspan="8" class="text-center p-4 fs-5 text-muted">
                        <i class="bi bi-inbox me-2"></i> No se encontraron órdenes de reparación.
                    </td>
                </tr>
            </c:if>
        </tbody>
    </table>
</div>

<jsp:include page="/vistas/tecnico/comun/footerTecnico.jsp" />