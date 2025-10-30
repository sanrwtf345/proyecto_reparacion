<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<%--
  1. Incluimos el HEADER.
  Le pasamos el atributo 'titulo' (que viene del servlet)
  al parámetro 'tituloPagina' que espera el header.
--%>
<jsp:include page="/vistas/admin/comun/headerAdmin.jsp">
    <jsp:param name="tituloPagina" value="${titulo}"/>
</jsp:include>


<%--
  INICIO DEL CONTENIDO ESPECÍFICO DE ESTA PÁGINA
  (El div 'container' ya no es necesario, lo provee el header)
--%>
<div class="card shadow-lg mx-auto" style="max-width: 600px;">
    <div class="card-header bg-danger text-white">

        <%-- TÍTULO DINÁMICO (obtenido del servlet) --%>
        <h3 class="mb-0">${titulo}</h3>

    </div>
    <div class="card-body">

        <%-- Mensaje de error --%>
        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger">${requestScope.error}</div>
            <c:remove var="error" scope="request"/>
        </c:if>

        <form action="<%= request.getContextPath() %>/UsuariosController" method="POST">

            <%-- **CORRECCIÓN CLAVE: El valor de 'action' ahora es dinámico** --%>
            <input type="hidden" name="action"
                   value="<c:choose><c:when test="${not empty requestScope.usuario and requestScope.usuario.idUsuario > 0}">actualizar</c:when><c:otherwise>guardar</c:otherwise></c:choose>">

            <%-- Campo Oculto para ID (Solo en Edición) --%>
            <c:if test="${not empty requestScope.usuario and requestScope.usuario.idUsuario > 0}">
                <input type="hidden" name="idUsuario" value="${usuario.idUsuario}">
            </c:if>

            <%-- ########## SECCIÓN MODIFICADA ########## --%>
            <div class="mb-3">
                <label for="correoElectronico" class="form-label">Correo Electrónico (Login)</label>
                <%-- Valor Pre-llenado --%>
                <input type="email" class="form-control" id="correoElectronico" name="correoElectronico" required
                       value="${usuario.correoElectronico}">
            </div>
            <%-- ########################################## --%>


            <div class="row">
                <div class="col-md-6 mb-3">
                    <label for="nombre" class="form-label">Nombre</label>
                    <input type="text" class="form-control" id="nombre" name="nombre" required
                           value="${usuario.nombre}">
                </div>
                <div class="col-md-6 mb-3">
                    <label for="apellido" class="form-label">Apellido</label>
                    <input type="text" class="form-control" id="apellido" name="apellido" required
                           value="${usuario.apellido}">
                </div>
            </div>

            <div class="row">
                <div class="col-md-6 mb-3">
                    <label for="password" class="form-label">Contraseña</label>
                    <input type="password" class="form-control" id="password" name="password"
                           <%-- Contraseña: Requerida solo si es nuevo registro --%>
                           <c:if test="${empty requestScope.usuario.idUsuario}">
                               required
                           </c:if>
                           placeholder="${not empty usuario.idUsuario ? 'Dejar vacío para NO cambiar' : 'Requerida para registrar'}">
                </div>
                <div class="col-md-6 mb-3">
                    <label for="rol" class="form-label">Rol</label>
                    <select class="form-select" id="rol" name="rol" required>
                        <option value="" disabled
                            <c:if test="${empty usuario.rol}">selected</c:if>
                            >Seleccione el rol</option>

                        <%-- Pre-seleccionar el rol --%>
                        <option value="ADMIN" ${usuario.rol eq 'ADMIN' ? 'selected' : ''}>Administrador</option>
                        <option value="TECNICO" ${usuario.rol eq 'TECNICO' ? 'selected' : ''}>Técnico</o ption>


                    </select>
                </div>
            </div>

            <div class="d-grid gap-2 mt-4">
                <%-- Texto del Botón Dinámico --%>
                <button type="submit" class="btn btn-danger btn-lg">
                    <c:choose>
                        <c:when test="${not empty requestScope.usuario and requestScope.usuario.idUsuario > 0}">
                            Guardar Cambios
                        </c:when>
                        <c:otherwise>
                            Registrar Usuario
                        </c:otherwise>
                    </c:choose>
                </button>
                <a href="<%= request.getContextPath() %>/UsuariosController?action=listar" class="btn btn-secondary">
                    Cancelar y Volver al Listado
                </a>
            </div>
        </form>
    </div>
</div>
<%--
  FIN DEL CONTENIDO ESPECÍFICO
--%>


<%--
  2. Incluimos el FOOTER.
--%>
<jsp:include page="/vistas/admin/comun/footerAdmin.jsp" />