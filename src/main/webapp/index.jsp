 <!DOCTYPE html>
 <html lang="es">
 <head>
     <meta charset="UTF-8">
     <meta name="viewport" content="width=device-width, initial-scale=1.0">
     <title>Página de Inicio con Login</title>
     <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">

     <style>
         /* Hace que el cuerpo ocupe al menos el 100% del alto de la ventana (viewport height) */
         body {
             min-height: 100vh;
             background-color: #f8f9fa; /* Color de fondo claro de Bootstrap */
         }
         /* Estilos opcionales para la tarjeta de login */
         .login-card {
             max-width: 400px; /* Ancho máximo de la tarjeta */
             width: 90%; /* Ancho en pantallas pequeñas */
         }
     </style>
 </head>
 <body class="d-flex align-items-center justify-content-center">

     <div class="login-card card p-4 shadow-lg">
         <h2 class="card-title text-center mb-4">Iniciar Sesión</h2>

         <form>
             <div class="mb-3">
                 <label for="inputEmail" class="form-label">Correo Electrónico</label>
                 <input type="email" class="form-control" id="inputEmail" placeholder="nombre@ejemplo.com" required>
             </div>

             <div class="mb-3">
                 <label for="inputPassword" class="form-label">Contraseña</label>
                 <input type="password" class="form-control" id="inputPassword" placeholder="Ingresa tu contraseña" required>
             </div>

             <div class="mb-3 form-check">
                 <input type="checkbox" class="form-check-input" id="checkRemember">
                 <label class="form-check-label" for="checkRemember">Recordarme</label>
             </div>

             <button type="submit" class="btn btn-primary w-100">Entrar</button>
         </form>
     </div>

     <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESbAA55NDzOxhy9Gkc0wU2rD6pA1UqB2jH0C0C" crossorigin="anonymous"></script>
 </body>
 </html>
