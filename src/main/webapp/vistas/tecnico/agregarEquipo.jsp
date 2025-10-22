<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registrar Equipo (Cliente Existente)</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>
        body { background-color: #f0f4f7; }
        .card-shadow { box-shadow: 0 0 30px rgba(0, 0, 0, 0.1); }
        .card-header-custom {
            background-color: #ffc107; /* Color warning para destacar la acción */
            border-radius: 10px 10px 0 0 !important;
            color: #343a40; /* Texto oscuro */
        }
    </style>
</head>
<body>

    <div class="container mt-5">
        <div class="card card-shadow mx-auto" style="max-width: 800px; border-radius: 10px;">
            <div class="card-header card-header-custom text-center">
                <h3 class="mb-0 fw-bold"><i class="bi bi-person-check-fill me-2"></i>Registrar Equipo para Cliente Existente</h3>
            </div>
            <div class="card-body p-4">

                <%-- Mensaje de Error (si el Servlet regresa debido a fallos de validación) --%>
                <c:if test="${not empty requestScope.error}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>${requestScope.error}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                    <c:remove var="error" scope="request"/>
                </c:if>

                <%-- Formulario POST: Envia datos al EquipoController --%>
                <form action="<%= request.getContextPath() %>/EquipoController" method="POST">

                    <input type="hidden" name="action" value="guardarNuevoEquipo">

                    <h5 class="mb-3 text-secondary border-bottom pb-2"><i class="bi bi-search me-2"></i>Seleccionar Cliente</h5>
                    <div class="mb-3">
                        <label for="idCliente" class="form-label fw-bold">Cliente <span class="text-danger">*</span></label>
                        <select class="form-select" id="idCliente" name="idCliente" required>
                            <option value="" disabled <c:if test="${empty param.idCliente}">selected</c:if>>Seleccione un Cliente</option>
                            <c:forEach var="cliente" items="${requestScope.listaClientes}">
                                <option value="${cliente.idCliente}"
                                        <c:if test="${param.idCliente == cliente.idCliente}">selected</c:if>>
                                    ${cliente.nombre} ${cliente.apellido} (Tel: ${cliente.telefono})
                                </option>
                            </c:forEach>
                        </select>
                        <div class="form-text">Si el cliente no aparece, debe registrarlo primero en el menú principal.</div>
                    </div>

                    <hr class="my-4">

                    <h5 class="mb-3 text-secondary border-bottom pb-2"><i class="bi bi-laptop me-2"></i>Datos del Equipo</h5>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="tipoEquipo" class="form-label fw-bold">Tipo de Equipo <span class="text-danger">*</span></label>
                            <select class="form-select" id="tipoEquipo" name="tipoEquipo" required>
                                <option value="" disabled <c:if test="${empty param.tipoEquipo}">selected</c:if>>Seleccione</option>
                                <option value="PORTATIL" <c:if test="${param.tipoEquipo == 'PORTATIL'}">selected</c:if>>Laptop / Notebook</option>
                                <option value="ESCRITORIO" <c:if test="${param.tipoEquipo == 'ESCRITORIO'}">selected</c:if>>PC de Escritorio</option>
                                <option value="MOVIL" <c:if test="${param.tipoEquipo == 'MOVIL'}">selected</c:if>>Teléfono / Tablet</option>
                                <option value="OTRO" <c:if test="${param.tipoEquipo == 'OTRO'}">selected</c:if>>Otro (Impresora, Monitor, etc.)</option>
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
                            <label for="numSerie" class="form-label fw-bold">Nro. de Serie (S/N)</label>
                            <input type="text" class="form-control" id="numSerie" name="numSerie" value="${param.numSerie}">
                        </div>
                    </div>

                    <h5 class="mt-4 mb-3 text-secondary border-bottom pb-2"><i class="bi bi-exclamation-octagon-fill me-2"></i>Falla Reportada</h5>
                    <div class="mb-3">
                        <label for="problemaReportado" class="form-label fw-bold">Descripción de la Falla <span class="text-danger">*</span></label>
                        <textarea class="form-control" id="problemaReportado" name="problemaReportado" rows="4" required>${param.problemaReportado}</textarea>
                        <div class="form-text">Detalle la falla reportada. Esta descripción se usará para la orden inicial.</div>
                    </div>

                    <div class="d-grid gap-2 mt-5">
                        <button type="submit" class="btn btn-warning btn-lg text-dark fw-bold">
                            <i class="bi bi-save-fill me-2"></i>Registrar Equipo y Abrir Orden
                        </button>
                        <a href="<%= request.getContextPath() %>/vistas/tecnico/menuTecnico.jsp" class="btn btn-outline-secondary">
                            <i class="bi bi-x-circle me-2"></i>Cancelar y Volver al Menú
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>