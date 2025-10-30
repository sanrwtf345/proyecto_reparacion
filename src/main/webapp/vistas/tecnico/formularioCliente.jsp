<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
  1. Incluimos el HEADER de Técnico (el de Bootstrap).
  Pasamos el título dinámico al header.
--%>
<jsp:include page="/vistas/tecnico/comun/headerTecnico.jsp">
    <jsp:param name="tituloPagina" value="${cliente.idCliente == 0 ? 'Registrar Nuevo Cliente' : 'Editar Cliente'}" />
</jsp:include>


<%--
  INICIO DEL CONTENIDO DE LA PÁGINA
  (Usamos la estructura de Card de Bootstrap)
--%>
<div class="card shadow-lg mx-auto" style="max-width: 700px;">

    <%-- Título en el Card Header (usamos bg-dark para el tema de técnico) --%>
    <div class="card-header bg-dark text-white">
        <h3 id="form-title" class="mb-0">
            <c:choose>
                <c:when test="${cliente.idCliente == 0}">Registrar Nuevo Cliente</c:when>
                <c:otherwise>Editar Cliente N° ${cliente.idCliente}</c:otherwise>
            </c:choose>
        </h3>
    </div>

    <div class="card-body p-4">

        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger" role="alert" aria-live="assertive">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${requestScope.error}
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/ClienteController" method="POST" aria-labelledby="form-title">

            <c:choose>
                <c:when test="${cliente.idCliente == 0}">
                    <input type="hidden" name="action" value="guardar">
                </c:when>
                <c:otherwise>
                    <input type="hidden" name="action" value="actualizar">
                    <input type="hidden" name="idCliente" value="${cliente.idCliente}">
                </c:otherwise>
            </c:choose>

            <c:if test="${cliente.idCliente != 0}">
                <div class="mb-3">
                    <label for="idClienteDisplay" class="form-label">ID Cliente</label>
                    <input type="text" id="idClienteDisplay" value="${cliente.idCliente}"
                           readonly class="form-control bg-light" aria-readonly="true">
                </div>
            </c:if>

            <div class="mb-3">
                <label for="nombre" class="form-label">Nombre</label>
                <input type="text" id="nombre" name="nombre" value="${cliente.nombre}"
                       required class="form-control" maxlength="50" aria-required="true">
            </div>

            <div class="mb-3">
                <label for="apellido" class="form-label">Apellido</label>
                <input type="text" id="apellido" name="apellido" value="${cliente.apellido}"
                       required class="form-control" maxlength="50" aria-required="true">
            </div>

            <div class="mb-3">
                <label for="telefono" class="form-label">Teléfono</label>
                <input type="tel" id="telefono" name="telefono" value="${cliente.telefono}"
                       required class="form-control" maxlength="15" aria-required="true">
            </div>

            <div class="mb-3">
                <label for="email" class="form-label">Email</label>
                <input type="email" id="email" name="email" value="${cliente.email}"
                       class="form-control" maxlength="100">
            </div>

            <div class="d-flex justify-content-between align-items-center pt-3 border-top mt-4">
                <a href="${pageContext.request.contextPath}/ClienteController?action=listar"
                   class="btn btn-secondary"
                   role="button"
                   aria-label="Cancelar y Volver al Listado de Clientes">
                   Volver al Listado
                </a>

                <button type="submit"
                        class="btn btn-primary btn-lg" <%-- bg-indigo-600 es ahora btn-primary --%>
                        role="button"
                        aria-label='<c:choose><c:when test="${cliente.idCliente == 0}">Registrar el nuevo Cliente</c:when><c:otherwise>Guardar los Cambios del Cliente N° ${cliente.idCliente}</c:otherwise></c:choose>'>
                    <c:choose>
                        <c:when test="${cliente.idCliente == 0}">Registrar Cliente</c:when>
                        <c:otherwise>Guardar Cambios</c:otherwise>
                    </c:choose>
                </button>
            </div>

        </form>
    </div>
</div>
<%-- FIN DEL CONTENIDO DE LA PÁGINA --%>


<%-- 2. Incluimos el FOOTER de Técnico (el de Bootstrap) --%>
<jsp:include page="/vistas/tecnico/comun/footerTecnico.jsp" />