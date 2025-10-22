<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- El título cambia dinámicamente según el modo -->
    <title><c:choose><c:when test="${cliente.idCliente == 0}">Registrar Nuevo Cliente</c:when><c:otherwise>Editar Cliente N° ${cliente.idCliente}</c:otherwise></c:choose></title>
    <!-- Incluye Tailwind CSS CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f3f4f6; }
    </style>
</head>
<body class="p-4 sm:p-8">
    <div class="max-w-xl mx-auto bg-white shadow-2xl rounded-2xl p-6 lg:p-10">

        <!-- Título que cambia según la acción -->
        <h1 class="text-2xl font-bold text-gray-800 mb-6 border-b pb-2">
            <c:choose>
                <c:when test="${cliente.idCliente == 0}">Registrar Nuevo Cliente</c:when>
                <c:otherwise>Editar Cliente N° ${cliente.idCliente}</c:otherwise>
            </c:choose>
        </h1>

        <!-- Mensajes de Error (si el Servlet falla en POST) -->
        <c:if test="${not empty requestScope.error}">
            <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6 rounded-lg" role="alert">
                <p class="font-bold">Error de Validación</p>
                <p>${requestScope.error}</p>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/ClienteController" method="POST" class="space-y-6">

            <!-- Bloque clave: Define la acción del POST (guardar o actualizar) -->
            <c:choose>
                <c:when test="${cliente.idCliente == 0}">
                    <!-- Modo Creación: La acción será 'guardar' -->
                    <input type="hidden" name="action" value="guardar">
                </c:when>
                <c:otherwise>
                    <!-- Modo Edición: La acción será 'actualizar' y enviamos el ID -->
                    <input type="hidden" name="action" value="actualizar">
                    <input type="hidden" name="idCliente" value="${cliente.idCliente}">
                </c:otherwise>
            </c:choose>

            <!-- Campo ID (Solo visible si es Edición) -->
            <c:if test="${cliente.idCliente != 0}">
                <div>
                    <label for="idClienteDisplay" class="block text-sm font-medium text-gray-700 mb-1">ID Cliente</label>
                    <input type="text" id="idClienteDisplay" value="${cliente.idCliente}"
                           readonly class="w-full px-4 py-2 border border-gray-200 bg-gray-50 rounded-lg text-gray-600 cursor-not-allowed">
                </div>
            </c:if>

            <!-- Campo Nombre -->
            <div>
                <label for="nombre" class="block text-sm font-medium text-gray-700 mb-1">Nombre</label>
                <input type="text" id="nombre" name="nombre" value="${cliente.nombre}"
                       required class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500 transition duration-150"
                       maxlength="50">
            </div>

            <!-- Campo Apellido -->
            <div>
                <label for="apellido" class="block text-sm font-medium text-gray-700 mb-1">Apellido</label>
                <input type="text" id="apellido" name="apellido" value="${cliente.apellido}"
                       required class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500 transition duration-150"
                       maxlength="50">
            </div>

            <!-- Campo Teléfono -->
            <div>
                <label for="telefono" class="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
                <input type="tel" id="telefono" name="telefono" value="${cliente.telefono}"
                       required class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500 transition duration-150"
                       maxlength="15">
            </div>

            <!-- Campo Email -->
            <div>
                <label for="email" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input type="email" id="email" name="email" value="${cliente.email}"
                       class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500 transition duration-150"
                       maxlength="100">
            </div>

            <!-- Botones de Acción -->
            <div class="flex justify-between items-center pt-4">
                <a href="${pageContext.request.contextPath}/ClienteController?action=listar"
                   class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-lg shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 transition duration-150">
                   Volver al Listado
                </a>

                <button type="submit"
                        class="inline-flex items-center px-6 py-2 border border-transparent rounded-lg shadow-sm text-base font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition duration-150 transform hover:scale-105">
                    <!-- Texto del botón cambia según el modo -->
                    <c:choose>
                        <c:when test="${cliente.idCliente == 0}">Registrar Cliente</c:when>
                        <c:otherwise>Guardar Cambios</c:otherwise>
                    </c:choose>
                </button>
            </div>

        </form>
    </div>
</body>
</html>