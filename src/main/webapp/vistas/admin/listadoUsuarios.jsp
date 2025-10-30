<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<%--
  1. Incluimos el HEADER.
  Le pasamos el título "Gestión de Usuarios" para el <title> de la página.
--%>
<jsp:include page="/vistas/admin/comun/headerAdmin.jsp">
    <jsp:param name="tituloPagina" value="Gestión de Usuarios"/>
</jsp:include>


<%--
  INICIO DEL CONTENIDO ESPECÍFICO DE ESTA PÁGINA
  (Ya no necesitamos el <div class="container..."> porque está en el header)
--%>
<h1 class="mb-4" id="listado-title">Gestión de Usuarios</h1>

<%-- Contenedor de mensajes de estado con rol ARIA 'alert' para que sean leídos --%>
<div role="alert" aria-live="assertive">
    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger">${sessionScope.error}</div>
        <c:remove var="error" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success">${sessionScope.success}</div>
        <c:remove var="success" scope="session"/>
    </c:if>
</div>

<%-- Tabla de listado de usuarios --%>
<table class="table table-striped table-bordered" aria-labelledby="listado-title">
    <thead class="table-dark">
        <tr>
            <th>ID</th>
            <th>Correo Electrónico</th>
            <th>Nombre Completo</th>
            <th>Rol</th>
            <th>Acciones</th>
        </tr>
    </thead>
    <tbody>
        <%-- Iteramos sobre la lista de usuarios obtenida del Servlet --%>
        <c:forEach var="user" items="${requestScope.listaUsuarios}">
            <tr>
                <td>${user.idUsuario}</td>
                <td>${user.correoElectronico}</td>
                <td>${user.nombre} ${user.apellido}</td>
                <td>${user.rol}</td>
                <td>
                    <%-- Solo mostramos el botón de eliminar si NO es la cuenta del admin logueado --%>
                    <c:if test="${user.idUsuario != sessionScope.usuarioLogueado.idUsuario}">
                        <a href="<%= request.getContextPath() %>/UsuariosController?action=eliminar&idUsuario=${user.idUsuario}"
                           class="btn btn-danger btn-sm"
                           onclick="return confirm('¿Estás seguro de que deseas eliminar a ${user.correoElectronico}?');"
                           aria-label="Eliminar usuario ${user.correoElectronico}">
                            Eliminar
                        </a>
                    </c:if>

                    <a href="<%= request.getContextPath() %>/UsuariosController?action=editar&idUsuario=${user.idUsuario}"
                       class="btn btn-info btn-sm text-white"
                       aria-label="Editar usuario ${user.correoElectronico}">Editar</a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<a href="<%= request.getContextPath() %>/vistas/admin/menuAdmin.jsp" class="btn btn-secondary" role="button">Volver al Menú</a>

<%--
  FIN DEL CONTENIDO ESPECÍFICO
--%>


<%--
  2. Incluimos el FOOTER.
--%>
<jsp:include page="/vistas/admin/comun/footerAdmin.jsp" />