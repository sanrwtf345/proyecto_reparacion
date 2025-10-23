<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Paso 2: Registrar Equipo y Orden</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>
        body { background-color: #e9ecef; }
        .card-shadow { box-shadow: 0 0 30px rgba(0, 0, 0, 0.1); }
        .card-header-custom {
            background-color: #198754; /* Color success de Bootstrap */
            border-radius: 10px 10px 0 0 !important;
        }
        .cliente-info {
            background-color: #e7f1ff;
            padding: 15px;
            border-radius: 8px;
            border-left: 5px solid #007bff;
        }
    </style>
</head>
<body>
    <%-- ARIA: role="main" para el contenido principal --%>
    <div class="container mt-5" role="main">
        <div class="card card-shadow mx-auto" style="max-width: 800px; border-radius: 10px;">
            <div class="card-header card-header-custom text-white">
                <%-- ARIA: Se añade ID al título para asociarlo al formulario --%>
                <h3 class="mb-0 text-center" id="main-form-title"><i class="bi bi-gear-fill me-2" aria-hidden="true"></i>Paso 2: Registrar Equipo y Abrir Orden</h3>
            </div>
            <div class="card-body p-4">

                <%-- Muestra el nombre del cliente para confirmación --%>
                <%-- ARIA: Se utiliza role="region" para agrupar información contextual --%>
                <div class="cliente-info mb-4" role="region" aria-label="Información del cliente para esta orden">
                    <p class="mb-0 fw-bold text-dark">Registrando equipo para:</p>
                    <h4 class="mb-0 text-primary">${requestScope.cliente.nombre} ${requestScope.cliente.apellido}</h4>
                </div>

                <%-- Muestra el mensaje de error si el Servlet lo envía --%>
                <c:if test="${not empty requestScope.error}">
                    <%-- ARIA: role="alert" y aria-live="assertive" para notificar el error inmediatamente --%>
                    <div class="alert alert-danger alert-dismissible fade show" role="alert" aria-live="assertive">
                        <i class="bi bi-exclamation-triangle-fill me-2" aria-hidden="true"></i>${requestScope.error}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar alerta de error"></button>
                    </div>
                    <c:remove var="error" scope="request"/>
                </c:if>

                <%-- Formulario POST: Envia datos de equipo y la orden inicial al ClienteEquipoController --%>
                <%-- ARIA: Asociar el formulario con el título principal --%>
                <form action="<%= request.getContextPath() %>/ClienteEquipoController" method="POST" aria-labelledby="main-form-title">

                    <%-- Campos Ocultos (sin cambios) --%>
                    <input type="hidden" name="action" value="guardarEquipoYOrden">
                    <input type="hidden" name="idCliente" value="${requestScope.cliente.idCliente}">

                    <h5 class="mb-3 text-secondary border-bottom pb-2"><i class="bi bi-boxes me-2" aria-hidden="true"></i>Datos del Equipo</h5>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="tipo" class="form-label fw-bold">Tipo de Equipo <span class="text-danger">*</span></label>
                            <%-- ARIA: aria-required="true" en el select requerido --%>
                            <select class="form-select" id="tipo" name="tipo" required aria-required="true">
                                <option value="" disabled <c:if test="${empty param.tipo}">selected</c:if>>Seleccione</option>
                                <option value="PORTATIL" <c:if test="${param.tipo == 'PORTATIL'}">selected</c:if>>Laptop / Notebook</option>
                                <option value="ESCRITORIO" <c:if test="${param.tipo == 'ESCRITORIO'}">selected</c:if>>PC de Escritorio</option>
                                <option value="MOVIL" <c:if test="${param.tipo == 'MOVIL'}">selected</c:if>>Teléfono / Tablet</option>
                                <option value="OTRO" <c:if test="${param.tipo == 'OTRO'}">selected</c:if>>Otro (Impresora, Monitor, etc.)</option>
                            </select>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="marca" class="form-label fw-bold">Marca</label>
                            <input type="text" class="form-control" id="marca" name="marca" value="${param.marca}">
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="modelo" class="form-label fw-bold">Modelo</label>
                            <input type="text" class="form-control" id="modelo" name="modelo" value="${param.modelo}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="nroSerie" class="form-label fw-bold">Nro. de Serie (S/N)</label>
                            <input type="text" class="form-control" id="nroSerie" name="nroSerie" value="${param.nroSerie}">
                        </div>
                    </div>

                    <h5 class="mt-4 mb-3 text-secondary border-bottom pb-2"><i class="bi bi-journal-text me-2" aria-hidden="true"></i>Falla y Observaciones</h5>
                    <div class="mb-3">
                        <label for="descripcionFalla" class="form-label fw-bold">Descripción de la Falla <span class="text-danger">*</span></label>
                        <%-- ARIA: aria-required y aria-describedby para el textarea requerido y su texto de ayuda --%>
                        <textarea class="form-control" id="descripcionFalla" name="descripcionFalla" rows="3" required aria-required="true" aria-describedby="descripcionFallaHelp">${param.descripcionFalla}</textarea>
                        <div id="descripcionFallaHelp" class="form-text">Detalle la falla reportada por el cliente. Esto será el diagnóstico inicial de la orden.</div>
                    </div>

                    <div class="mb-3">
                        <label for="observaciones" class="form-label fw-bold">Observaciones y Accesorios</label>
                        <%-- ARIA: aria-describedby para el texto de ayuda --%>
                        <textarea class="form-control" id="observaciones" name="observaciones" rows="2" aria-describedby="observacionesHelp">${param.observaciones}</textarea>
                        <div id="observacionesHelp" class="form-text">Ej: Trae cargador, falta batería, falta clave de acceso. **Nota:** Este campo no se usa en la BD por el momento, pero es bueno registrarlo para el técnico.</div>
                    </div>

                    <div class="d-grid gap-2 mt-5">
                        <%-- ARIA: role="button" y una etiqueta clara para la acción --%>
                        <button type="submit" class="btn btn-success btn-lg" role="button" aria-label="Finalizar el registro del equipo y abrir la nueva orden de reparación">
                            <i class="bi bi-check-circle-fill me-2" aria-hidden="true"></i>Finalizar Registro y Abrir Orden
                        </button>
                        <%-- ARIA: role="button" y etiqueta para el enlace de cancelación --%>
                        <a href="<%= request.getContextPath() %>/vistas/tecnico/menuTecnico.jsp" class="btn btn-outline-secondary" role="button" aria-label="Cancelar el proceso y volver al Menú del Técnico">
                            <i class="bi bi-x-circle me-2" aria-hidden="true"></i>Cancelar y Volver al Menú
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>