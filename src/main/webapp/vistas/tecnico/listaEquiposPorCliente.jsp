<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<%-- 1. Incluimos el HEADER de Técnico --%>
<%-- HEADER DINÁMICO --%>
<c:choose>
    <c:when test="${sessionScope.usuarioLogueado.rol eq 'ADMIN'}">
        <jsp:include page="/vistas/admin/comun/headerAdmin.jsp"><jsp:param name="tituloPagina" value="Equipos de ${cliente.nombre}"/></jsp:include>
    </c:when>
    <c:otherwise>
        <jsp:include page="/vistas/tecnico/comun/headerTecnico.jsp"><jsp:param name="tituloPagina" value="Equipos de ${cliente.nombre}"/></jsp:include>
    </c:otherwise>
</c:choose>


<%-- INICIO DEL CONTENIDO --%>
<h1 class="mb-4 text-center">Gestión de Equipos</h1>
<h4 class="mb-4 text-center text-muted">
  Cliente: ${cliente.nombre} ${cliente.apellido}
</h4>

<%-- Mensajes de feedback --%>
<c:if test="${not empty sessionScope.error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="bi bi-exclamation-triangle-fill me-2"></i>${sessionScope.error}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
    <c:remove var="error" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.success}">
    <div class="alert alert-success alert-dismissible fade show" role="alert">
        <i class="bi bi-check-circle-fill me-2"></i>${sessionScope.success}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
    <c:remove var="success" scope="session"/>
</c:if>

<div class="mb-3 d-flex justify-content-between align-items-center">
     <a href="${pageContext.request.contextPath}/EquipoController?action=mostrarAgregarEquipo&idCliente=${cliente.idCliente}" class="btn btn-success shadow-sm"
        role="button">
        <i class="bi bi-plus-circle me-1"></i> Agregar Nuevo Equipo
    </a>
    <a href="${pageContext.request.contextPath}/ClienteController?action=listar" class="btn btn-secondary"
       role="button">
        <i class="bi bi-arrow-left me-1"></i> Volver a Clientes
    </a>
</div>

<div class="table-responsive bg-white p-3 rounded shadow-sm">
    <table class="table table-hover table-striped table-sm align-middle">
        <thead class="table-dark">
            <tr>
                <th>ID</th>
                <th>Tipo</th>
                <th>Marca</th>
                <th>Modelo</th>
                <th>Nro. Serie</th>
                <th class="text-center">Acciones</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="equipo" items="${requestScope.listaEquipos}">
                <tr>
                    <td>${equipo.idEquipo}</td>
                    <td>${equipo.tipoEquipo}</td>
                    <td>${equipo.marca}</td>
                    <td>${equipo.modelo}</td>
                    <td>${equipo.numeroSerie}</td>
                    <td class="text-center">

                        <%-- === BOTÓN NUEVO: CREAR ORDEN (Verde) === --%>
                        <a href="${pageContext.request.contextPath}/ReparacionController?action=nueva&idEquipo=${equipo.idEquipo}"
                           class="btn btn-success btn-sm text-white me-1"
                           title="Iniciar nueva reparación para este equipo">
                            <i class="bi bi-tools"></i>
                        </a>

                        <%-- === BOTÓN NUEVO: VER HISTORIAL (Gris) === --%>
                        <a href="${pageContext.request.contextPath}/EquipoController?action=verHistorial&idEquipo=${equipo.idEquipo}"
                           class="btn btn-secondary btn-sm text-white me-1"
                           title="Ver historial de reparaciones">
                            <i class="bi bi-clock-history"></i>
                        </a>

                        <%-- BOTÓN EDITAR (Azul/Cian) --%>
                        <a href="${pageContext.request.contextPath}/EquipoController?action=mostrarEditarEquipo&idEquipo=${equipo.idEquipo}"
                           class="btn btn-info btn-sm text-white me-1"
                           title="Editar datos del equipo">
                            <i class="bi bi-pencil-fill"></i>
                        </a>

                        <%-- BOTÓN ELIMINAR (Rojo) --%>
                        <a href="${pageContext.request.contextPath}/EquipoController?action=eliminarEquipo&idEquipo=${equipo.idEquipo}&idCliente=${cliente.idCliente}"
                           class="btn btn-danger btn-sm"
                           onclick="return confirm('¿Eliminar equipo y su historial?');"
                           title="Eliminar equipo">
                            <i class="bi bi-trash-fill"></i>
                        </a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty requestScope.listaEquipos}">
                 <tr>
                    <td colspan="6" class="text-center text-muted p-4">
                        <i class="bi bi-laptop me-2"></i> Este cliente no tiene equipos registrados.
                    </td>
                </tr>
            </c:if>
        </tbody>
    </table>
</div>

<jsp:include page="/vistas/tecnico/comun/footerTecnico.jsp" />