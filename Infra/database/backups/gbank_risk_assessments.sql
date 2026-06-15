-- MySQL dump 10.13  Distrib 9.4.0, for macos26.0 (arm64)
--
-- Host: localhost    Database: gbank_risk_assessments
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
-- Table structure for table `risk_decision_reason`
--

DROP TABLE IF EXISTS `risk_decision_reason`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `risk_decision_reason` (
  `trace_id` bigint NOT NULL,
  `reason_code` varchar(255) DEFAULT NULL,
  KEY `FKbqlg4os7srsd7idikt9rfv38i` (`trace_id`),
  CONSTRAINT `FKbqlg4os7srsd7idikt9rfv38i` FOREIGN KEY (`trace_id`) REFERENCES `risk_decision_trace` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `risk_decision_reason`
--

LOCK TABLES `risk_decision_reason` WRITE;
/*!40000 ALTER TABLE `risk_decision_reason` DISABLE KEYS */;
/*!40000 ALTER TABLE `risk_decision_reason` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `risk_decision_trace`
--

DROP TABLE IF EXISTS `risk_decision_trace`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `risk_decision_trace` (
  `ml_probability` double DEFAULT NULL,
  `evaluated_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `model_name` varchar(255) DEFAULT NULL,
  `model_version` varchar(255) DEFAULT NULL,
  `policy_version` varchar(255) DEFAULT NULL,
  `trained_at` varchar(255) DEFAULT NULL,
  `transaction_id` varchar(255) NOT NULL,
  `final_status` enum('APPROVED','DECLINED','INITIATED','REVIEW') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_evaluated_at` (`evaluated_at`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `risk_decision_trace`
--

LOCK TABLES `risk_decision_trace` WRITE;
/*!40000 ALTER TABLE `risk_decision_trace` DISABLE KEYS */;
INSERT INTO `risk_decision_trace` VALUES (0.18220890016108798,'2026-05-26 11:45:51.293749',1,'fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30',NULL,'8bf60512-1610-4b46-8409-e6f63bc012e4','APPROVED'),(0.13507388697806313,'2026-05-26 11:45:51.395543',2,'fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30',NULL,'0e366729-175c-4827-a3f9-2cc1675e14ed','APPROVED'),(0.13696065678586816,'2026-05-26 11:45:51.436237',3,'fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30',NULL,'0b1d36f0-494b-4c0d-8259-60b27e5a2586','APPROVED'),(0.13924850189792423,'2026-05-26 11:45:51.471585',4,'fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30',NULL,'e91f49db-c8d7-4a13-b48c-34c2254f269c','APPROVED'),(0.14178689629700122,'2026-05-26 11:45:51.506346',5,'fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30',NULL,'e6ed8db0-530a-43e2-bf53-a49fbe0645e2','APPROVED'),(0.1445072413366958,'2026-05-26 11:45:51.543291',6,'fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30',NULL,'0b2410eb-f4e4-4ca1-bf9c-c1a01717d915','APPROVED'),(0.1473728154865661,'2026-05-26 11:45:52.024323',7,'fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30',NULL,'7c1af14c-73d4-40fd-b193-b3a4635ed4c3','APPROVED'),(0.15036186646835614,'2026-05-26 11:45:52.069885',8,'fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30',NULL,'53394db6-340c-4fad-88d5-374450708f07','APPROVED'),(0.15346058421602696,'2026-05-26 11:45:52.104824',9,'fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30',NULL,'68867492-c783-4258-8337-256ea9d99988','APPROVED');
/*!40000 ALTER TABLE `risk_decision_trace` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_device_history`
--

DROP TABLE IF EXISTS `user_device_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_device_history` (
  `first_seen_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_seen_at` datetime(6) DEFAULT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_device` (`user_id`,`device_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_device_history`
--

LOCK TABLES `user_device_history` WRITE;
/*!40000 ALTER TABLE `user_device_history` DISABLE KEYS */;
INSERT INTO `user_device_history` VALUES ('2026-05-26 11:44:07.226000',1,'2026-05-26 11:44:07.226000','DEV-ANDROID-MOBILE','ea078591-64c4-48e9-b00d-c12ad5904846');
/*!40000 ALTER TABLE `user_device_history` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-27 14:58:46
