-- MySQL dump 10.13  Distrib 9.4.0, for macos26.0 (arm64)
--
-- Host: localhost    Database: gbank_notification_service
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
-- Table structure for table `failed_notification_events`
--

DROP TABLE IF EXISTS `failed_notification_events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `failed_notification_events` (
  `failed_at` datetime(6) NOT NULL,
  `id` binary(16) NOT NULL,
  `transaction_id` varchar(100) DEFAULT NULL,
  `source_topic` varchar(150) NOT NULL,
  `error_message` longtext,
  `payload` longtext,
  PRIMARY KEY (`id`),
  KEY `idx_failed_at` (`failed_at`),
  KEY `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `failed_notification_events`
--

LOCK TABLES `failed_notification_events` WRITE;
/*!40000 ALTER TABLE `failed_notification_events` DISABLE KEYS */;
/*!40000 ALTER TABLE `failed_notification_events` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `failed_transaction_events`
--

DROP TABLE IF EXISTS `failed_transaction_events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `failed_transaction_events` (
  `failed_at` datetime(6) DEFAULT NULL,
  `id` binary(16) NOT NULL,
  `source_topic` varchar(255) DEFAULT NULL,
  `transaction_id` varchar(255) DEFAULT NULL,
  `error_message` text,
  `payload` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `failed_transaction_events`
--

LOCK TABLES `failed_transaction_events` WRITE;
/*!40000 ALTER TABLE `failed_transaction_events` DISABLE KEYS */;
/*!40000 ALTER TABLE `failed_transaction_events` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_processed`
--

DROP TABLE IF EXISTS `notification_processed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_processed` (
  `processed_at` datetime(6) NOT NULL,
  `event_id` binary(16) NOT NULL,
  PRIMARY KEY (`event_id`),
  KEY `idx_processed_at` (`processed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_processed`
--

LOCK TABLES `notification_processed` WRITE;
/*!40000 ALTER TABLE `notification_processed` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification_processed` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-27 14:58:34
