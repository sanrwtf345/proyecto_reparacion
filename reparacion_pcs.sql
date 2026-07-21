/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- ======================================================
-- 1. Table structure and data for table `usuarios`
-- ======================================================
DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
                            `id_usuario` int NOT NULL AUTO_INCREMENT,
                            `correo_electronico` varchar(50) NOT NULL,
                            `password` varchar(255) NOT NULL,
                            `nombre` varchar(50) NOT NULL,
                            `apellido` varchar(50) NOT NULL,
                            `rol` varchar(20) NOT NULL DEFAULT 'TECNICO',
                            PRIMARY KEY (`id_usuario`),
                            UNIQUE KEY `nombre_usuario` (`correo_electronico`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES
                           (1,'admin@taller.com','$2a$10$1XnehyIeB6BKJkP/kymkmeGb5fNq8XIld9N9xUnD54U6FKfqOmbfO','Admin','Principal','ADMIN'),
                           (2,'german@gmail.com','$2a$10$WnYgYf.PE0Bh9bAsQLiOsuPs50nGr/GlBJj9RixHvFeKibgZS0E9e','German','Romero','TECNICO'),
                           (11,'prueba@gmail.com','$2a$10$nkQsQ5f.kp7tYE023rKY6er5IXZH4TWlF6Ctv7N5TzyCaF4gZqsaS','Juan','Ramirez','TECNICO'),
                           (12,'prueba2@gmail.com','$2a$10$OBiUn1Hoo.8WtJ/pahjEpuM4pkgYSDr22FeTwrkfez6nz6iqas0ve','Ramiro','Gomez','TECNICO');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;


-- ======================================================
-- 2. Table structure and data for table `clientes`
-- ======================================================
DROP TABLE IF EXISTS `clientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clientes` (
                            `id_cliente` int NOT NULL AUTO_INCREMENT,
                            `nombre` varchar(50) NOT NULL,
                            `apellido` varchar(50) NOT NULL,
                            `telefono` varchar(20) DEFAULT NULL,
                            `email` varchar(100) DEFAULT NULL,
                            `id_usuario` int DEFAULT NULL,
                            PRIMARY KEY (`id_cliente`),
                            KEY `id_usuario` (`id_usuario`),
                            CONSTRAINT `clientes_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `clientes` WRITE;
/*!40000 ALTER TABLE `clientes` DISABLE KEYS */;
INSERT INTO `clientes` VALUES
                           (7,'Pablo Andres','Perez','3482222222','pablito@gmail.com',1),
                           (8,'Agustin','Fernandez','3482666666','prueba00@gmail.com',2),
                           (9,'Miguel','DeNardo','3482999999','prueba01@gmail.com',2),
                           (10,'Roman ','Gomez','3482777777','prueba02@gmail.com',2),
                           (11,'Marga','Romero','34821111111','prueba03@gmail.com',2);
/*!40000 ALTER TABLE `clientes` ENABLE KEYS */;
UNLOCK TABLES;


-- ======================================================
-- 3. Table structure and data for table `equipo`
-- ======================================================
DROP TABLE IF EXISTS `equipo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `equipo` (
                          `id_equipo` int NOT NULL AUTO_INCREMENT,
                          `id_cliente` int NOT NULL,
                          `tipo_equipo` varchar(50) NOT NULL,
                          `marca` varchar(50) DEFAULT NULL,
                          `modelo` varchar(50) DEFAULT NULL,
                          `num_serie` varchar(100) DEFAULT NULL,
                          `problema_reportado` text NOT NULL,
                          `fecha_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id_equipo`),
                          KEY `id_cliente` (`id_cliente`),
                          CONSTRAINT `equipo_ibfk_1` FOREIGN KEY (`id_cliente`) REFERENCES `clientes` (`id_cliente`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `equipo` WRITE;
/*!40000 ALTER TABLE `equipo` DISABLE KEYS */;
INSERT INTO `equipo` VALUES
                         (12,7,'ESCRITORIO','Sin Marca','Full tower','S/N','asdasd','2025-11-25 16:05:00'),
                         (13,9,'PORTATIL','Lenovo','Ideapad','0025895548896354','No enciende','2025-12-05 16:17:01'),
                         (14,10,'PORTATIL','Hp','s/n','98412555745857845','No carga','2025-12-05 16:17:52'),
                         (15,8,'ESCRITORIO','Sin Marca','Mid Tower','S/N','El cooler hace un ruido extraño','2025-12-05 16:18:26'),
                         (16,11,'PORTATIL','ASUS','VivoBook','789875255889','La pantalla no funciona','2025-12-05 16:19:43');
/*!40000 ALTER TABLE `equipo` ENABLE KEYS */;
UNLOCK TABLES;


-- ======================================================
-- 4. Table structure and data for table `reparacion`
-- ======================================================
DROP TABLE IF EXISTS `reparacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reparacion` (
                              `id_reparacion` int NOT NULL AUTO_INCREMENT,
                              `id_equipo` int NOT NULL,
                              `id_usuario` int NOT NULL,
                              `fecha_diagnostico` date DEFAULT NULL,
                              `diagnostico_final` text,
                              `estado` varchar(50) NOT NULL DEFAULT 'PENDIENTE_DIAGNOSTICO',
                              `costo_repuestos` decimal(10,2) DEFAULT '0.00',
                              `costo_mano_obra` decimal(10,2) DEFAULT '0.00',
                              `presupuesto_total` decimal(10,2) DEFAULT '0.00',
                              `fecha_entrega_estimada` date DEFAULT NULL,
                              `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id_reparacion`),
                              KEY `id_equipo` (`id_equipo`),
                              KEY `id_usuario` (`id_usuario`),
                              CONSTRAINT `reparacion_ibfk_1` FOREIGN KEY (`id_equipo`) REFERENCES `equipo` (`id_equipo`),
                              CONSTRAINT `reparacion_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `reparacion` WRITE;
/*!40000 ALTER TABLE `reparacion` DISABLE KEYS */;
INSERT INTO `reparacion` VALUES
                             (1,12,2,'2025-11-28','Fuente quemada ','TERMINADO',1.74,1.53,3.27,'2025-12-01','2025-11-28 14:15:51'),
                             (2,12,2,'2025-11-28','Fuente quemada','PENDIENTE',98000.00,15000.00,113000.00,'2025-12-02','2025-11-28 14:18:26'),
                             (3,13,2,'2025-12-05','Ram quemada, se requiere cambio de la pieza','PENDIENTE',30000.00,15000.00,45000.00,'2025-12-10','2025-12-05 16:20:58'),
                             (4,15,2,NULL,'Cambio de ventilador del disipador ya que se encontraba defectuoso','EN_PROCESO',10000.00,10000.00,20000.00,NULL,'2025-12-05 16:21:49'),
                             (5,14,2,'2025-12-05','Cambio de bateria','CANCELADO',60000.00,20000.00,80000.00,'2025-12-16','2025-12-05 16:22:52');
/*!40000 ALTER TABLE `reparacion` ENABLE KEYS */;
UNLOCK TABLES;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;