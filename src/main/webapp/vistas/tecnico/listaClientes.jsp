<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<%-- 1. Incluimos el HEADER de Técnico --%>
<jsp:include page="/vistas/tecnico/comun/headerTecnico.jsp">
    <jsp:param name="tituloPagina" value="Gestión de Clientes"/>
</jsp:include>


<%-- INICIO DEL CONTENIDO DE LA PÁGINA --%>
<h1 class="mb-4 text-center">Gestión de Clientes</h1>

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
     <a href="${pageContext.request.contextPath}/ClienteController?action=crear" class="btn btn-success shadow-sm"
        role="button" aria-label="Registrar un nuevo cliente en el sistema">
        <i class="bi bi-person-plus-fill me-1" aria-hidden="true"></i> Registrar Nuevo Cliente
    </a>
    <a href="${pageContext.request.contextPath}/vistas/tecnico/menuTecnico.jsp" class="btn btn-secondary"
       role="button" aria-label="Volver al menú principal del técnico">
        <i class="bi bi-arrow-left me-1" aria-hidden="true"></i> Volver al Menú
    </a>
</div>

<div class="table-responsive bg-white p-3 rounded shadow-sm" role="region" aria-labelledby="client-list-caption">
    <h2 id="client-list-caption" class="sr-only">Listado de Clientes Registrados</h2>
    <table class="table table-hover table-striped table-sm">
        <thead class="table-dark">
            <tr>
                <th scope="col">ID</th>
                <th scope="col">Nombre Completo</th>
                <th scope="col">Teléfono</th>
                <th scope="col">Email</th>
                <th scope="col" class="text-center">Acciones</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="cliente" items="${requestScope.listaClientes}">
                <tr>
                    <td>${cliente.idCliente}</td>
                    <td>${cliente.nombre} ${cliente.apellido}</td>
                    <td>${cliente.telefono}</td>
                    <td>${cliente.email}</td>
                    <td class="text-center">

                        <%-- === INICIO DEL CAMBIO === --%>

                        <a href="${pageContext.request.contextPath}/EquipoController?action=listarPorCliente&idCliente=${cliente.idCliente}"
                           class="btn btn-primary btn-sm me-2"
                           aria-label="Ver equipos del cliente ${cliente.nombre} ${cliente.apellido}">
                            <i class="bi bi-laptop" aria-hidden="true"></i> Ver Equipos
                        </a>

                        <%-- === FIN DEL CAMBIO === --%>


                        <a href="${pageContext.request.contextPath}/ClienteController?action=editar&idCliente=${cliente.idCliente}"
                           class="btn btn-info btn-sm text-white me-2"
                           aria-label="Editar datos del cliente ${cliente.nombre} ${cliente.apellido}">
                            <i class="bi bi-pencil-fill" aria-hidden="true"></i> Editar
                        </a>

                        <a href="${pageContext.request.contextPath}/ClienteController?action=eliminar&idCliente=${cliente.idCliente}"
                           class="btn btn-danger btn-sm"
                           onclick="return confirm('ATENCIÓN: ¿Estás seguro de ELIMINAR al cliente ${cliente.nombre} y TODOS sus equipos y órdenes asociadas? Esta acción es irreversible.');"
                           aria-label="Eliminar cliente ${cliente.nombre} ${cliente.apellido} y todos los datos asociados">
                            <i class="bi bi-trash-fill" aria-hidden="true"></i> Eliminar
                        </a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty requestScope.listaClientes}">
                 <tr>
                    <td colspan="5" class="text-center text-muted p-4">
                        <i class="bi bi-person-lines-fill me-2" aria-hidden="true"></i> No hay clientes registrados en la base de datos.
                    </td>
                </tr>
            </c:if>
        </tbody>
    </table>
</div>
<%-- FIN DEL CONTENIDO DE LA PÁGINA --%>


<%-- 2. Incluimos el FOOTER de Técnico --%>
<jsp:include page="/vistas/tecnico/comun/footerTecnico.jsp" />