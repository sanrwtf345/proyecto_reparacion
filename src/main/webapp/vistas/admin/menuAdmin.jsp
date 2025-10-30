<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<%--
  1. Incluimos el HEADER.
  Le pasamos el parámetro "tituloPagina" para el <title>
--%>
<jsp:include page="/vistas/admin/comun/headerAdmin.jsp">
    <jsp:param name="tituloPagina" value="Panel de Control"/>
</jsp:include>


<%--
  INICIO DEL CONTENIDO ESPECÍFICO DE ESTA PÁGINA
  (Todo lo que estaba dentro de <div class="container mt-5" role="main">)
--%>
<h1 class="mb-4 text-center">Panel de Control General</h1>

<%-- ARIA: role="region" para agrupar contenido relacionado (Gestión de Usuarios) --%>
<h2 class="border-bottom pb-2 mb-4 text-secondary" id="gestion-usuarios-heading">Gestión de Usuarios</h2>
<div class="row row-cols-1 row-cols-md-2 g-4 mb-5" role="region" aria-labelledby="gestion-usuarios-heading">

    <div class="col">
        <div class="card card-menu h-100 shadow-sm border-danger">
            <div class="card-body">
                <h5 class="card-title text-danger"><i class="bi bi-person-plus-fill me-2" aria-hidden="true"></i>Registrar Técnico</h5>
                <p class="card-text">Crear nuevas cuentas para técnicos y administradores con credenciales y roles.</p>
                <a href="<%= request.getContextPath() %>/UsuariosController?action=formularioRegisUsuario"
                   class="btn btn-danger w-100"
                   aria-describedby="reg-tec-desc"
                   role="button">
                    Alta de Usuario
                </a>
                <p id="reg-tec-desc" class="visually-hidden">Enlace para registrar un nuevo técnico o administrador.</p>
            </div>
        </div>
    </div>

    <div class="col">
        <div class="card card-menu h-100 shadow-sm border-info">
            <div class="card-body">
                <h5 class="card-title text-info"><i class="bi bi-list-ul me-2" aria-hidden="true"></i>Listar y Eliminar Usuarios</h5>
                <p class="card-text">Ver todos los usuarios registrados y gestionar la eliminación de cuentas.</p>
                <a href="<%= request.getContextPath() %>/UsuariosController?action=listar"
                   class="btn btn-info w-100"
                   aria-describedby="list-elim-desc"
                   role="button">
                    Ver Listado
                </a>
                <p id="list-elim-desc" class="visually-hidden">Enlace para acceder al listado de usuarios, donde se pueden ver, editar y eliminar cuentas.</p>
            </div>
        </div>
    </div>

</div>

<%-- ARIA: role="region" para agrupar contenido relacionado (Tareas Operativas) --%>
<h2 class="border-bottom pb-2 mb-4 text-secondary" id="tareas-operativas-heading">Tareas Operativas</h2>
<div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4" role="region" aria-labelledby="tareas-operativas-heading">

    <div class="col">
        <div class="card card-menu h-100 shadow-sm border-primary">
            <div class="card-body">
                <h5 class="card-title text-primary">Nueva Orden</h5>
                <p class="card-text">Registrar cliente, equipo y generar la orden de servicio.</p>
                <a href="${pageContext.request.contextPath}/ClienteEquipoController"
                   class="btn btn-primary w-100"
                   aria-describedby="nueva-orden-desc"
                   role="button">
                    Registrar Equipo
                </a>
                <p id="nueva-orden-desc" class="visually-hidden">Enlace para iniciar el registro de un nuevo equipo y una nueva orden de servicio.</p>
            </div>
        </div>
    </div>

    <div class="col">
        <div class="card card-menu h-100 shadow-sm border-warning">
            <div class="card-body">
                <h5 class="card-title text-warning">Órdenes Pendientes</h5>
                <p class="card-text">Ver listado de órdenes en espera de diagnóstico o presupuesto.</p>
                <a href="${pageContext.request.contextPath}/ReparacionController?action=listar"
                   class="btn btn-warning w-100 text-dark"
                   aria-describedby="ordenes-pendientes-desc"
                   role="button">
                    Ver Pendientes
                </a>
                 <p id="ordenes-pendientes-desc" class="visually-hidden">Enlace para ver el listado de todas las órdenes de servicio pendientes.</p>
            </div>
        </div>
    </div>

</div>
<%--
  FIN DEL CONTENIDO ESPECÍFICO
--%>


<%--
  2. Incluimos el FOOTER.
--%>
<jsp:include page="/vistas/admin/comun/footerAdmin.jsp" />