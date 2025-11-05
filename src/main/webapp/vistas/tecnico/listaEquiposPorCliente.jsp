<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<%-- 1. Incluimos el HEADER de Técnico --%>
<jsp:include page="/vistas/tecnico/comun/headerTecnico.jsp">
    <jsp:param name="tituloPagina" value="Equipos de ${cliente.nombre}"/>
</jsp:include>


<%-- INICIO DEL CONTENIDO DE LA PÁGINA --%>
<%-- Título dinámico con el nombre del cliente --%>
<h1 class="mb-4 text-center">Gestión de Equipos</h1>
<h4 class="mb-4 text-center text-muted">
  Cliente: ${cliente.nombre} ${cliente.apellido}
</h4>


<%-- Muestra mensajes de éxito/error de la sesión --%>
<c:if test="${not empty sessionScope.error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert" aria-live="polite">
        <i class="bi bi-exclamation-triangle-fill me-2" aria-hidden="true"></i>${sessionScope.error}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar mensaje de error"></button>
    </div>
    <c:remove var="error" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.success}">
    <div class="alert alert-success alert-dismissible fade show" role="alert" aria-live="polite">
        <i class="bi bi-check-circle-fill me-2" aria-hidden="true"></i>${sessionScope.success}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar mensaje de éxito"></button>
    </div>
    <c:remove var="success" scope="session"/>
</c:if>

<div class="mb-3 d-flex justify-content-between align-items-center">
     <%-- Botón para agregar OTRO equipo a ESTE cliente --%>
     <a href="${pageContext.request.contextPath}/EquipoController?action=mostrarAgregarEquipo" class="btn btn-success shadow-sm"
        role="button">
        <i class="bi bi-plus-circle me-1" aria-hidden="true"></i> Agregar Nuevo Equipo
    </a>
    <%-- Botón para volver a la lista de TODOS los clientes --%>
    <a href="${pageContext.request.contextPath}/ClienteController?action=listar" class="btn btn-secondary"
       role="button" aria-label="Volver al listado de clientes">
        <i class="bi bi-arrow-left me-1" aria-hidden="true"></i> Volver a Clientes
    </a>
</div>

<div class="table-responsive bg-white p-3 rounded shadow-sm" role="region" aria-labelledby="equipos-list-caption">
    <h2 id="equipos-list-caption" class="sr-only">Listado de Equipos del Cliente</h2>
    <table class="table table-hover table-striped table-sm">
        <thead class="table-dark">
            <tr>
                <th scope="col">ID Equipo</th>
                <th scope="col">Tipo</th>
                <th scope="col">Marca</th>
                <th scope="col">Modelo</th>
                <th scope="col">Nro. Serie</th>
                <th scope="col" class="text-center">Acciones</th>
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

                        <%-- === INICIO DEL CAMBIO === --%>

                        <%-- Botón de Editar (AHORA ACTIVADO) --%>
                        <a href="${pageContext.request.contextPath}/EquipoController?action=mostrarEditarEquipo&idEquipo=${equipo.idEquipo}"
                           class="btn btn-info btn-sm text-white me-2"
                           aria-label="Editar equipo ${equipo.marca} ${equipo.modelo}">
                            <i class="bi bi-pencil-fill" aria-hidden="true"></i> Editar
                        </a>

                        <%-- === FIN DEL CAMBIO === --%>

                        <%-- Botón de Eliminar (Paso 4) --%>
                        <a href="${pageContext.request.contextPath}/EquipoController?action=eliminarEquipo&idEquipo=${equipo.idEquipo}&idCliente=${cliente.idCliente}"
                           class="btn btn-danger btn-sm"
                           onclick="return confirm('ATENCIÓN: ¿Estás seguro de ELIMINAR este equipo (${equipo.marca} ${equipo.modelo}) y TODAS sus órdenes de reparación asociadas?');"
                           aria-label="Eliminar equipo ${equipo.marca} ${equipo.modelo}">
                            <i class="bi bi-trash-fill" aria-hidden="true"></i> Eliminar
                        </a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty requestScope.listaEquipos}">
                 <tr>
                    <td colspan="6" class="text-center text-muted p-4">
                        <i class="bi bi-laptop me-2" aria-hidden="true"></i> Este cliente no tiene equipos registrados.
                    </td>
                </tr>
            </c:if>
        </tbody>
    </table>
</div>
<%-- FIN DEL CONTENIDO DE LA PÁGINA --%>


<%-- 2. Incluimos el FOOTER de Técnico --%>
<jsp:include page="/vistas/tecnico/comun/footerTecnico.jsp" />