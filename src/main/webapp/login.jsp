 <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
 <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
 <%@ page isELIgnored="false" %>
 <!DOCTYPE html>
 <html lang="es">
 <head>
     <meta charset="UTF-8">
     <meta name="viewport" content="width=device-width, initial-scale=1.0">
     <title>Iniciar Sesión - Sistema de Reparaciones</title>
     <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
           integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">

     <style>
         body { min-height: 100vh; background-color: #f8f9fa; }
         .login-card { max-width: 400px; width: 90%; }
     </style>
 </head>
 <body class="d-flex align-items-center justify-content-center">

     <div class="login-card card p-4 shadow-lg">
         <h2 class="card-title text-center mb-4">Iniciar Sesión</h2>

         <%-- Esto sigue igual, muestra el error enviado desde el LoginServlet --%>
         <c:if test="${not empty error}">
             <div class="alert alert-danger" role="alert">
                 ${error}
             </div>
         </c:if>

         <form action="LoginServlet" method="post">

             <%-- AQUÍ ESTÁN LOS CAMBIOS --%>
             <div class="mb-3">
                 <label for="inputCorreo" class="form-label">Correo Electrónico</label>
                 <input type="email" class="form-control" id="inputCorreo" name="correoElectronico"
                        placeholder="Ingresa tu correo" required>
             </div>

             <div class="mb-3">
                 <label for="inputPassword" class="form-label">Contraseña</label>
                 <input type="password" class="form-control" id="inputPassword" name="password"
                        placeholder="Ingresa tu contraseña" required>
             </div>

             <div class="mb-3 form-check">
                 <input type="checkbox" class="form-check-input" id="checkRemember">
                 <label class="form-check-label" for="checkRemember">Recordarme</label>
             </div>

             <button type="submit" class="btn btn-primary w-100">Entrar</button>
         </form>
     </div>

     <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
             integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESbAA55NDzOxhy9Gkc0wU2rD6pA1UqB2jH0C0C"
             crossorigin="anonymous"></script>
 </body>
 </html>
