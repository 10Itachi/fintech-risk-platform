-- MySQL dump 10.13  Distrib 9.4.0, for macos26.0 (arm64)
--
-- Host: localhost    Database: gbank_user_db
-- ------------------------------------------------------
-- Server version	9.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `created_at` datetime(6) NOT NULL,
  `user_id` binary(16) NOT NULL,
  `email` varchar(255) NOT NULL,
  `keycloak_user_id` varchar(255) NOT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) NOT NULL,
  `is_active` enum('ACTIVE','BLOCKED','CLOSED','INACTIVE','SUSPENDED') DEFAULT NULL,
  `role` enum('ADMIN','RISKCALLER','USER') NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKetoqbe1mjqwls3an7lwjaynu7` (`keycloak_user_id`),
  UNIQUE KEY `UK9q63snka3mdh91as4io72espi` (`phone_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('2026-05-24 12:05:05.354959',_binary '\ð#\éA¥²\ÌO+uB','user11@gmail.com','a5b2e317-1d54-4fbf-a303-a1f8629f30b6','0599390311','user11','ACTIVE','USER'),('2026-05-26 07:46:01.749186',_binary '%\ó\\‹C\â„`¢\ö]\\9\'','user13@gmail.com','a5c507a3-6301-4cb5-818b-e6a8967a0bb0','9982814713','user13','ACTIVE','USER'),('2026-05-22 09:11:59.063666',_binary 'x|ŠsVtBª¨M[£/‰Nf','user2@gmail.com','ea078591-64c4-48e9-b00d-c12ad5904846','5681918572','user2','ACTIVE','USER'),('2026-05-22 05:46:29.165838',_binary '| ºnC´°)h2Á¬E\Ì','admin1@gmail.com','eafb4b3c-886e-40d3-83ae-8f00f56c1932',NULL,'admin1','ACTIVE','ADMIN'),('2026-05-25 04:55:46.323157',_binary 'ŒL\ô\â\ßO\çºÉªB|\à“','user12@gmail.com','ac6e1811-f450-47b1-91b9-1a6dbda8860b','9982814712','user12','ACTIVE','ADMIN'),('2026-05-22 09:11:27.982193',_binary '£m\Ôå‘žF`´\"¾Z£qµ','user4@gmail.com','3091704e-4336-407d-85ad-8dfa90740095','5681918574','user4','ACTIVE','USER'),('2026-05-22 05:46:29.211970',_binary '\Ó\á8GŠ\ÉC´dh\ì§','user1@gmail.com','e4bb2a8a-a6f9-4dd0-82f1-2a745ab58ecc',NULL,'user1','ACTIVE','ADMIN'),('2026-05-22 09:10:56.530414',_binary 'Ö€1\×(ZA\á±\õ|¡l\Ý>E','user6@gmail.com','8e23e818-a8b6-4e37-a9a8-932cc82d4a05','5681918576','user6','ACTIVE','USER'),('2026-05-22 09:10:14.617754',_binary '\ð\î&¼mA\÷ŠNÁ€\Äs%','user8@gmail.com','56c8c910-91d0-4a5b-a177-82887ed8450a','5681918578','user8','ACTIVE','USER');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-27 14:59:16
