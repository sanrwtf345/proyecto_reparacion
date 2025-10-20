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
</head>
<body>
    <div class="container mt-5">
        <div class="card shadow-lg mx-auto" style="max-width: 800px;">
            <div class="card-header bg-success text-white">
                <h3 class="mb-0">Paso 2: Registrar Equipo y Abrir Orden</h3>
            </div>
            <div class="card-body">

                <%-- Muestra el nombre del cliente para confirmación --%>
                <h4 class="mb-4">Cliente: <span class="text-primary">${requestScope.cliente.nombre} ${requestScope.cliente.apellido}</span></h4>

                <c:if test="${not empty requestScope.error}">
                    <div class="alert alert-danger">${requestScope.error}</div>
                    <c:remove var="error" scope="request"/>
                </c:if>

                <%-- Formulario POST: Envia datos de equipo y la orden inicial al ClienteEquipoController --%>
                <form action="<%= request.getContextPath() %>/ClienteEquipoController" method="POST">

                    <%-- Campo Oculto: ID del Cliente (necesario para la BD) --%>
                    <input type="hidden" name="action" value="guardarEquipoYOrden">
                    <input type="hidden" name="idCliente" value="${requestScope.cliente.idCliente}">

                    <h5 class="mb-3 text-secondary">Datos del Equipo</h5>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="tipo" class="form-label">Tipo de Equipo *</label>
                            <select class="form-select" id="tipo" name="tipo" required>
                                <option value="" disabled selected>Seleccione</option>
                                <option value="PORTATIL">Laptop / Notebook</option>
                                <option value="ESCRITORIO">PC de Escritorio</option>
                                <option value="MOVIL">Teléfono / Tablet</option>
                                <option value="OTRO">Otro (Impresora, Monitor, etc.)</option>
                            </select>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="marca" class="form-label">Marca</label>
                            <input type="text" class="form-control" id="marca" name="marca">
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="modelo" class="form-label">Modelo</label>
                            <input type="text" class="form-control" id="modelo" name="modelo">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="nroSerie" class="form-label">Nro. de Serie (S/N)</label>
                            <input type="text" class="form-control" id="nroSerie" name="nroSerie">
                        </div>
                    </div>

                    <h5 class="mt-4 mb-3 text-secondary">Falla y Observaciones</h5>
                    <div class="mb-3">
                        <label for="descripcionFalla" class="form-label">Descripción de la Falla *</label>
                        <textarea class="form-control" id="descripcionFalla" name="descripcionFalla" rows="3" required></textarea>
                    </div>

                    <div class="mb-3">
                        <label for="observaciones" class="form-label">Observaciones y Accesorios</label>
                        <textarea class="form-control" id="observaciones" name="observaciones" rows="2"></textarea>
                        <div class="form-text">Ej: Trae cargador, falta batería, clave de acceso.</div>
                    </div>

                    <div class="d-grid gap-2 mt-4">
                        <button type="submit" class="btn btn-success btn-lg">Finalizar Registro y Abrir Orden</button>
                        <a href="<%= request.getContextPath() %>/vistas/tecnico/menuTecnico.jsp" class="btn btn-secondary">Cancelar y Volver al Menú</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>