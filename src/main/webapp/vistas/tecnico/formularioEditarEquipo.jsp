<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<%-- 1. Incluimos el HEADER de Técnico --%>
<jsp:include page="/vistas/tecnico/comun/headerTecnico.jsp">
    <jsp:param name="tituloPagina" value="Editar Equipo N° ${equipo.idEquipo}"/>
</jsp:include>


<%-- INICIO DEL CONTENIDO DE LA PÁGINA --%>
<div class="card card-shadow mx-auto" style="max-width: 800px; border-radius: 10px;">

    <%-- Usamos un color 'info' (cian) para la cabecera de edición --%>
    <div class="card-header bg-info text-white">
        <h3 class="mb-0 text-center" id="main-form-title">
            <i class="bi bi-pencil-fill me-2" aria-hidden="true"></i>Editar Equipo
        </h3>
    </div>

    <div class="card-body p-4">

        <%-- Mostramos el cliente al que pertenece (no es editable) --%>
        <div class="cliente-info mb-4" role="region" aria-label="Información del cliente">
            <p class="mb-0 fw-bold text-dark">Editando equipo perteneciente a:</p>
            <%-- Asumimos que el servlet enviará el objeto 'equipo' con el 'cliente' dentro --%>
            <h4 class="mb-0 text-primary">${equipo.cliente.nombre} ${equipo.cliente.apellido}</h4>
        </div>

        <%-- Muestra el mensaje de error si el Servlet lo envía --%>
        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-danger" role="alert" aria-live="assertive">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>${requestScope.error}
            </div>
        </c:if>

        <%-- El formulario apunta a 'EquipoController' --%>
        <form action="<%= request.getContextPath() %>/EquipoController" method="POST" aria-labelledby="main-form-title">

            <%-- Campos Ocultos para el 'doPost' --%>
            <input type="hidden" name="action" value="actualizarEquipo">
            <input type="hidden" name="idEquipo" value="${equipo.idEquipo}">
            <%-- Importante: Enviamos el idCliente de vuelta para saber a dónde redirigir --%>
            <input type="hidden" name="idCliente" value="${equipo.cliente.idCliente}">


            <h5 class="mb-3 text-secondary border-bottom pb-2"><i class="bi bi-boxes me-2" aria-hidden="true"></i>Datos del Equipo</h5>

            <div class="row">
                <div class="col-md-6 mb-3">
                    <label for="tipo" class="form-label fw-bold">Tipo de Equipo <span class="text-danger">*</span></label>
                    <select class="form-select" id="tipo" name="tipoEquipo" required aria-required="true">
                        <option value="" disabled>Seleccione</option>
                        <%-- Lógica JSTL para pre-seleccionar la opción guardada --%>
                        <option value="PORTATIL" ${equipo.tipoEquipo == 'PORTATIL' ? 'selected' : ''}>Laptop / Notebook</option>
                        <option value="ESCRITORIO" ${equipo.tipoEquipo == 'ESCRITORIO' ? 'selected' : ''}>PC de Escritorio</option>
                        <option value="MOVIL" ${equipo.tipoEquipo == 'MOVIL' ? 'selected' : ''}>Teléfono / Tablet</option>
                        <option value="OTRO" ${equipo.tipoEquipo == 'OTRO' ? 'selected' : ''}>Otro (Impresora, Monitor, etc.)</option>
                    </select>
                </div>
                <div class="col-md-6 mb-3">
                    <label for="marca" class="form-label fw-bold">Marca</label>
                    <%-- Usamos 'value' para pre-rellenar los campos --%>
                    <input type="text" class="form-control" id="marca" name="marca" value="${equipo.marca}">
                </div>
            </div>

            <div class="row">
                <div class="col-md-6 mb-3">
                    <label for="modelo" class="form-label fw-bold">Modelo</label>
                    <input type="text" class="form-control" id="modelo" name="modelo" value="${equipo.modelo}">
                </div>
                <div class="col-md-6 mb-3">
                    <label for="nroSerie" class="form-label fw-bold">Nro. de Serie (S/N)</label>
                    <input type="text" class="form-control" id="nroSerie" name="numSerie" value="${equipo.numeroSerie}">
                </div>
            </div>

            <h5 class="mt-4 mb-3 text-secondary border-bottom pb-2"><i class="bi bi-journal-text me-2" aria-hidden="true"></i>Falla y Observaciones</h5>

            <div class="mb-3">
                <label for="descripcionFalla" class="form-label fw-bold">Descripción de Falla (Opcional)</label>
                <%-- En 'textarea' el valor se pone entre las etiquetas --%>
                <textarea class="form-control" id="descripcionFalla" name="problemaReportado" rows="3">${equipo.problemaReportado}</textarea>
            </div>

            <div class="d-grid gap-2 mt-5">
                <button type="submit" class="btn btn-info btn-lg text-white" role="button" aria-label="Guardar cambios del equipo">
                    <i class="bi bi-check-circle-fill me-2" aria-hidden="true"></i>Guardar Cambios
                </button>

                <%-- El botón Cancelar nos regresa a la lista de equipos de ESE cliente --%>
                <a href="${pageContext.request.contextPath}/EquipoController?action=listarPorCliente&idCliente=${equipo.cliente.idCliente}"
                   class="btn btn-outline-secondary" role="button">
                    <i class="bi bi-x-circle me-2" aria-hidden="true"></i>Cancelar
                </a>
            </div>
        </form>
    </div>
</div>
<%-- FIN DEL CONTENIDO DE LA PÁGINA --%>


<%-- 2. Incluimos el FOOTER de Técnico --%>
<jsp:include page="/vistas/tecnico/comun/footerTecnico.jsp" />