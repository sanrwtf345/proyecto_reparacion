<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<%-- 1. Incluimos el HEADER de Admin --%>
<jsp:include page="/vistas/admin/comun/headerAdmin.jsp"><jsp:param name="tituloPagina" value="Panel de Control"/></jsp:include>


<%-- INICIO DEL CONTENIDO --%>
<h1 class="mb-4 text-center">Panel de Control General</h1>

<%-- ========================================================= --%>
<%-- BLOQUE DE MENSAJES (Igual que en Técnico) --%>
<%-- ========================================================= --%>
<c:if test="${not empty sessionScope.success}">
    <div class="alert alert-success alert-dismissible fade show" role="alert">
        <i class="bi bi-check-circle-fill me-2"></i>${sessionScope.success}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
    <c:remove var="success" scope="session"/>
</c:if>

<c:if test="${not empty sessionScope.error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="bi bi-exclamation-triangle-fill me-2"></i>${sessionScope.error}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
    <c:remove var="error" scope="session"/>
</c:if>


<%-- ======================================================================== --%>
<%-- SECCIÓN 1: GESTIÓN DE USUARIOS (EXCLUSIVO ADMIN) --%>
<%-- ======================================================================== --%>
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
            </div>
        </div>
    </div>

</div>

<%-- ======================================================================== --%>
<%-- SECCIÓN 2: GESTIÓN OPERATIVA (IGUAL AL TÉCNICO) --%>
<%-- ======================================================================== --%>
<h2 class="border-bottom pb-2 mb-4 text-secondary" id="gestion-operativa-heading">Gestión Operativa</h2>
<div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4" role="region" aria-labelledby="gestion-operativa-heading">

    <!-- 1. NUEVA ORDEN (Cliente Nuevo + Equipo) -->
    <div class="col">
        <div class="card card-menu h-100 shadow-sm border-primary">
            <div class="card-body card-body-custom">
                <h5 class="card-title text-primary"><i class="bi bi-file-earmark-plus-fill me-2"></i>Nuevo Cliente</h5>
                <p class="card-text">Registrar un nuevo cliente.</p>
                <a href="${pageContext.request.contextPath}/ClienteController?action=crear" class="btn btn-primary w-100 mt-auto">
                    Iniciar Registro de Cliente
                </a>
            </div>
        </div>
    </div>

    <!-- 2. EQUIPO EXISTENTE (Cliente Existente + Equipo Nuevo) -->
    <div class="col">
        <div class="card card-menu h-100 shadow-sm border-warning">
            <div class="card-body card-body-custom">
                <h5 class="card-title text-warning"><i class="bi bi-laptop me-2"></i>Equipo Existente</h5>
                <p class="card-text">Registrar un nuevo equipo y orden para un cliente ya registrado en el sistema.</p>
                <a href="${pageContext.request.contextPath}/EquipoController?action=mostrarAgregarEquipo" class="btn btn-warning w-100 text-dark mt-auto">
                    Registrar Equipo
                </a>
            </div>
        </div>
    </div>

    <!-- 3. GESTIÓN DE CLIENTES (NUEVO) -->
    <div class="col">
        <div class="card card-menu h-100 shadow-sm border-success">
            <div class="card-body card-body-custom">
                <h5 class="card-title text-success"><i class="bi bi-people-fill me-2"></i>Gestionar Clientes</h5>
                <p class="card-text">Administrar la lista de clientes (Crear, Editar datos o Eliminar clientes existentes).</p>
                <a href="${pageContext.request.contextPath}/ClienteController?action=listar" class="btn btn-success w-100 mt-auto">
                    Ir a la Gestión
                </a>
            </div>
        </div>
    </div>

    <!-- 4. ÓRDENES PENDIENTES -->
    <div class="col">
        <div class="card card-menu h-100 shadow-sm border-info">
            <div class="card-body card-body-custom">
                <h5 class="card-title text-info"><i class="bi bi-list-task me-2"></i>Órdenes Pendientes</h5>
                <p class="card-text">Ver listado de todas las órdenes de reparación activas en espera de diagnóstico o reparación.</p>
                <a href="${pageContext.request.contextPath}/ReparacionController?action=listar" class="btn btn-info w-100 text-white mt-auto">
                    Ver Pendientes
                </a>
            </div>
        </div>
    </div>

</div>

<%--
  2. Incluimos el FOOTER.
--%>
<jsp:include page="/vistas/admin/comun/footerAdmin.jsp" />