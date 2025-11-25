<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<%-- HEADER DINÁMICO: Elige el header según el rol del usuario --%>
<c:choose>
    <c:when test="${sessionScope.usuarioLogueado.rol eq 'ADMIN'}">
        <%-- Si es ADMIN, usamos el header rojo --%>
        <jsp:include page="/vistas/admin/comun/headerAdmin.jsp"><jsp:param name="tituloPagina" value="Gestión (Modo Admin)"/></jsp:include>
    </c:when>
    <c:otherwise>
        <%-- Si es TÉCNICO (u otro), usamos el header oscuro --%>
        <jsp:include page="/vistas/tecnico/comun/headerTecnico.jsp"><jsp:param name="tituloPagina" value="Gestión Operativa"/></jsp:include>
    </c:otherwise>
</c:choose>


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

<%-- BARRA DE HERRAMIENTAS: Búsqueda + Botones --%>
<div class="card mb-4 shadow-sm bg-light">
    <div class="card-body py-3">
        <form class="row g-3 align-items-center" action="${pageContext.request.contextPath}/ClienteController" method="GET">
            <input type="hidden" name="action" value="listar">

            <%-- Input de Búsqueda --%>
            <div class="col-auto">
                <label for="busquedaApellido" class="visually-hidden">Buscar por Apellido</label>
                <div class="input-group">
                    <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
                    <input type="text" id="busquedaApellido" name="busquedaApellido" class="form-control"
                           placeholder="Buscar apellido..." value="${requestScope.busquedaActual}">
                </div>
            </div>

            <%-- Botón Buscar --%>
            <div class="col-auto">
                <button type="submit" class="btn btn-primary">Buscar</button>
            </div>

            <%-- Botón Limpiar (Condicional) --%>
            <c:if test="${not empty requestScope.busquedaActual}">
                <div class="col-auto">
                    <a href="${pageContext.request.contextPath}/ClienteController?action=listar" class="btn btn-outline-secondary" aria-label="Limpiar filtro">
                        <i class="bi bi-x-lg"></i>
                    </a>
                </div>
            </c:if>

            <%-- Espaciador para empujar botones a la derecha --%>
            <div class="col"></div>

            <%-- Botones de Acción (Registrar y Volver) --%>
            <div class="col-auto">
                 <a href="${pageContext.request.contextPath}/ClienteController?action=crear" class="btn btn-success shadow-sm">
                    <i class="bi bi-person-plus-fill me-1"></i> Nuevo Cliente
                </a>
            </div>

            <div class="col-auto">
                <%-- BOTÓN VOLVER DINÁMICO --%>
                <c:choose>
                    <c:when test="${sessionScope.usuarioLogueado.rol eq 'ADMIN'}">
                        <a href="${pageContext.request.contextPath}/vistas/admin/menuAdmin.jsp" class="btn btn-secondary">
                            <i class="bi bi-arrow-left me-1"></i> Volver
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/vistas/tecnico/menuTecnico.jsp" class="btn btn-secondary">
                            <i class="bi bi-arrow-left me-1"></i> Volver
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </form>
    </div>
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

                        <a href="${pageContext.request.contextPath}/EquipoController?action=listarPorCliente&idCliente=${cliente.idCliente}"
                           class="btn btn-primary btn-sm me-2"
                           aria-label="Ver equipos del cliente ${cliente.nombre} ${cliente.apellido}">
                            <i class="bi bi-laptop" aria-hidden="true"></i> Ver Equipos
                        </a>

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