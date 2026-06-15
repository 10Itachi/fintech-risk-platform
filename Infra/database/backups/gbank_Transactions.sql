-- MySQL dump 10.13  Distrib 9.4.0, for macos26.0 (arm64)
--
-- Host: localhost    Database: gbank_Transactions
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
-- Table structure for table `outbox_event`
--

DROP TABLE IF EXISTS `outbox_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `outbox_event` (
  `event_version` int NOT NULL,
  `retry_count` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `next_retry_at` datetime(6) DEFAULT NULL,
  `sent_at` datetime(6) DEFAULT NULL,
  `aggregate_id` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `aggregate_type` varchar(100) NOT NULL,
  `event_type` varchar(100) NOT NULL,
  `correlation_id` varchar(255) DEFAULT NULL,
  `payload` longtext NOT NULL,
  `status` enum('DEAD','FAILED','PENDING','SENT') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_status_next_retry` (`status`,`next_retry_at`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_aggregate_id` (`aggregate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `outbox_event`
--

LOCK TABLES `outbox_event` WRITE;
/*!40000 ALTER TABLE `outbox_event` DISABLE KEYS */;
INSERT INTO `outbox_event` VALUES (1,0,'2026-05-26 11:45:51.478102','2026-05-26 11:45:51.478102','2026-05-26 11:45:52.863290',_binary '\ÈI\€\»\◊J¥å4\¬%O&ú',_binary '{oæ\ÂÜM	É¨R\ÁØU&K','TRANSACTION','TRANSACTION_FINALIZED','Co-relationId:97da2325-234c-4dc8-abc1-7855a436a22c','{\"eventId\":\"057b6fbe-e586-4d09-83ac-52e7af55264b\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:51.478102Z\",\"transactionId\":\"e91f49db-c8d7-4a13-b48c-34c2254f269c\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:97da2325-234c-4dc8-abc1-7855a436a22c\"}','SENT'),(1,0,'2026-05-26 11:45:51.407636','2026-05-26 11:45:51.407636','2026-05-26 11:45:52.777639',_binary '6g)\\H\'£˘,¡g^\Ì',_binary '\Z\ÎÿäMjF æƒù®˘m','TRANSACTION','TRANSACTION_FINALIZED','Co-relationId:2a63a4b5-4d76-40f3-9c89-da0217943997','{\"eventId\":\"1aebd88a-4d6a-4620-be04-c49d16a8f96d\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:51.407636Z\",\"transactionId\":\"0e366729-175c-4827-a3f9-2cc1675e14ed\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:2a63a4b5-4d76-40f3-9c89-da0217943997\"}','SENT'),(1,0,'2026-05-26 11:45:52.033188','2026-05-26 11:45:52.033188','2026-05-26 11:45:52.912539',_binary '|\Z\ÒLs\‘@˝±ì≥§c^\‘\√',_binary '=¸ W\«ADÇI\–m\ÃC•','TRANSACTION','TRANSACTION_FINALIZED','Co-relationId:473c7e0f-33d5-44f0-82ce-9c1e35701be1','{\"eventId\":\"3d18fc20-57c7-4144-8214-49d06dcc43a5\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:52.033188Z\",\"transactionId\":\"7c1af14c-73d4-40fd-b193-b3a4635ed4c3\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:473c7e0f-33d5-44f0-82ce-9c1e35701be1\"}','SENT'),(1,0,'2026-05-26 11:45:51.443420','2026-05-26 11:45:51.443420','2026-05-26 11:45:52.803037',_binary '6\IKL\rÇY`≤~Z%Ü',_binary 's›¥êKHDò#Å]i\ÔK˛','TRANSACTION','TRANSACTION_FINALIZED','Co-relationId:b5f2491a-07db-4932-aa25-d91019069d27','{\"eventId\":\"73ddb490-4b48-4403-9823-815d69ef4bfe\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:51.443420Z\",\"transactionId\":\"0b1d36f0-494b-4c0d-8259-60b27e5a2586\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:b5f2491a-07db-4932-aa25-d91019069d27\"}','SENT'),(1,0,'2026-05-26 11:45:52.076877','2026-05-26 11:45:52.076877','2026-05-26 11:45:52.923257',_binary 'S9M∂4O≠à\’7DPpè',_binary 'ê|\‹S\€O“¶\—˚\›\Ò\‚≥','TRANSACTION','TRANSACTION_FINALIZED','Co-relationId:5de96ac3-c9f1-4f4c-a409-d630bde23d55','{\"eventId\":\"907c18dc-53db-4fd2-a61d-d1fbddf1e2b3\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:52.076877Z\",\"transactionId\":\"53394db6-340c-4fad-88d5-374450708f07\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:5de96ac3-c9f1-4f4c-a409-d630bde23d55\"}','SENT'),(1,0,'2026-05-26 11:45:51.344005','2026-05-26 11:45:51.344005','2026-05-26 11:45:52.691974',_binary 'ã\ˆKFÑ	\Ê\ˆ;¿\‰',_binary 'û?¥êQ\ﬂN)§5®hé\‚ß','TRANSACTION','TRANSACTION_FINALIZED','Co-relationId:fefd35cb-0b21-4573-a0b4-f1156072f4a6','{\"eventId\":\"9e3fb490-51df-4e29-a435-a8688ee2a70f\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:51.344005Z\",\"transactionId\":\"8bf60512-1610-4b46-8409-e6f63bc012e4\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:fefd35cb-0b21-4573-a0b4-f1156072f4a6\"}','SENT'),(1,0,'2026-05-26 11:45:51.550696','2026-05-26 11:45:51.550696','2026-05-26 11:45:52.898279',_binary '$\Î\Ù\‰L°øú¡†\Ÿ',_binary 'º1\Ùu\ÔwMó\Â\«˛û\ﬂ\≈','TRANSACTION','TRANSACTION_FINALIZED','Co-relationId:4aa563d3-fce2-4478-a24d-6182cf274297','{\"eventId\":\"bc31f475-ef77-4d06-97e5-c7fe9edf14c5\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:51.550696Z\",\"transactionId\":\"0b2410eb-f4e4-4ca1-bf9c-c1a01717d915\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:4aa563d3-fce2-4478-a24d-6182cf274297\"}','SENT'),(1,0,'2026-05-26 11:45:51.514543','2026-05-26 11:45:51.514543','2026-05-26 11:45:52.885451',_binary '\ÊÌç∞S\nC\‚øS§üæE\‚',_binary '\≈ﬂ∏•ìO´ªA\Ëi-`','TRANSACTION','TRANSACTION_FINALIZED','Co-relationId:7215e82a-8dab-49e8-963d-33e71c6277d6','{\"eventId\":\"c5dfb8a5-1d93-4fab-bb41-e8691e2d601f\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:51.514543Z\",\"transactionId\":\"e6ed8db0-530a-43e2-bf53-a49fbe0645e2\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:7215e82a-8dab-49e8-963d-33e71c6277d6\"}','SENT'),(1,0,'2026-05-26 11:45:52.112797','2026-05-26 11:45:52.112797','2026-05-26 11:45:52.933378',_binary 'hÜtí«ÉBXÉ7%n©Ÿôà',_binary 'Âôí\È\ ;F7íW\ı\ÏR\ÿ\—U','TRANSACTION','TRANSACTION_FINALIZED','Co-relationId:529630a2-a0a9-4d33-81e1-9755ea548dda','{\"eventId\":\"e59992e9-ca3b-4637-9257-f5ec52d8d155\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:52.112797Z\",\"transactionId\":\"68867492-c783-4258-8337-256ea9d99988\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:529630a2-a0a9-4d33-81e1-9755ea548dda\"}','SENT');
/*!40000 ALTER TABLE `outbox_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `risk_decision`
--

DROP TABLE IF EXISTS `risk_decision`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `risk_decision` (
  `fraud_probability` double DEFAULT NULL,
  `risk_score` int DEFAULT NULL,
  `evaluated_at` datetime(6) NOT NULL,
  `latency_ms` bigint DEFAULT NULL,
  `id` binary(16) NOT NULL,
  `transaction_id` binary(16) NOT NULL,
  `model_name` varchar(255) DEFAULT NULL,
  `model_version` varchar(255) DEFAULT NULL,
  `policy_version` varchar(255) DEFAULT NULL,
  `decision` enum('APPROVED','DECLINED','INITIATED','REVIEW') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `risk_decision`
--

LOCK TABLES `risk_decision` WRITE;
/*!40000 ALTER TABLE `risk_decision` DISABLE KEYS */;
INSERT INTO `risk_decision` VALUES (0.1445072413366958,NULL,'2026-05-26 11:45:51.550347',12230417,_binary '9\Ï\È∫¨Dáæí5/`[îd',_binary '$\Î\Ù\‰L°øú¡†\Ÿ','fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30','APPROVED'),(0.14178689629700122,NULL,'2026-05-26 11:45:51.514212',12926708,_binary ':q¿\ŒC∞Oƒ•ô\Ê\ı\ÈV',_binary '\ÊÌç∞S\nC\‚øS§üæE\‚','fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30','APPROVED'),(0.13696065678586816,NULL,'2026-05-26 11:45:51.443079',12088208,_binary 'ã\«\Ëg\ÕzH÷π\Ê\ı™Jß\0',_binary '6\IKL\rÇY`≤~Z%Ü','fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30','APPROVED'),(0.13507388697806313,NULL,'2026-05-26 11:45:51.407310',17836042,_binary 'è£ü2}KJÄæÿªL\ÂD',_binary '6g)\\H\'£˘,¡g^\Ì','fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30','APPROVED'),(0.15036186646835614,NULL,'2026-05-26 11:45:52.076353',11865125,_binary 'ï)ødúbN∞\∆\Œu≠\È',_binary 'S9M∂4O≠à\’7DPpè','fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30','APPROVED'),(0.15346058421602696,NULL,'2026-05-26 11:45:52.112503',12457292,_binary 'ß\‚q®G£ºqj\››†o',_binary 'hÜtí«ÉBXÉ7%n©Ÿôà','fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30','APPROVED'),(0.13924850189792423,NULL,'2026-05-26 11:45:51.477671',10997167,_binary ' •|m,TCõ--5?\Ù',_binary '\ÈI\€\»\◊J¥å4\¬%O&ú','fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30','APPROVED'),(0.1473728154865661,NULL,'2026-05-26 11:45:52.032853',14293416,_binary '\À“∑^LNï°\\nls',_binary '|\Z\ÒLs\‘@˝±ì≥§c^\‘\√','fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30','APPROVED'),(0.18220890016108798,NULL,'2026-05-26 11:45:51.340019',344856500,_binary '\ÁQ:∞}@1âO¨ ¥éß\„',_binary 'ã\ˆKFÑ	\Ê\ˆ;¿\‰','fraud_detection_logistic_regression','2.0.0-20260513-134555','2026-01-30','APPROVED');
/*!40000 ALTER TABLE `risk_decision` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `risk_reason_codes`
--

DROP TABLE IF EXISTS `risk_reason_codes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `risk_reason_codes` (
  `risk_decision_id` binary(16) NOT NULL,
  `reason_code` varchar(255) DEFAULT NULL,
  KEY `FK3a4wtfv67fc13n7smpmu0iq39` (`risk_decision_id`),
  CONSTRAINT `FK3a4wtfv67fc13n7smpmu0iq39` FOREIGN KEY (`risk_decision_id`) REFERENCES `risk_decision` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `risk_reason_codes`
--

LOCK TABLES `risk_reason_codes` WRITE;
/*!40000 ALTER TABLE `risk_reason_codes` DISABLE KEYS */;
/*!40000 ALTER TABLE `risk_reason_codes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions` (
  `amount` decimal(38,2) NOT NULL,
  `country` varchar(3) NOT NULL,
  `total_amount_24h_snapshot` decimal(38,2) DEFAULT NULL,
  `txn_count_24h_snapshot` int DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `transaction_time` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` binary(16) NOT NULL,
  `transaction_id` varchar(36) NOT NULL,
  `device_id` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `idempotency_key` varchar(255) DEFAULT NULL,
  `source_account` varchar(255) NOT NULL,
  `target_account` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) NOT NULL,
  `channel` enum('CARD','NET_BANKING','UPI') NOT NULL,
  `internal_status` enum('COMPLETED','FAILED','INITIATED','LEDGER_FAILED','LEDGER_PENDING','LEDGER_SUCCESS','PENDING_RISK','REVIEW_PENDING','RISK_APPROVED','RISK_REJECTED') NOT NULL,
  `transaction_status` enum('APPROVED','DECLINED','INITIATED','REVIEW') NOT NULL,
  `transaction_type` enum('DEPOSIT','TRANSFER','WITHDRAWAL') NOT NULL,
  PRIMARY KEY (`transaction_id`),
  UNIQUE KEY `UKe58eyb3fj0t2cl16q1ll436b7` (`idempotency_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
INSERT INTO `transactions` VALUES (5.00,'IN',15.00,3,'2026-05-26 11:45:51.428272','2026-05-26 11:44:07.226000','2026-05-26 11:45:51.443287',_binary '\ÍÖëd\ƒH\È∞\r¡*’êHF','0b1d36f0-494b-4c0d-8259-60b27e5a2586','DEV-ANDROID-MOBILE','user2@gmail.com','1e562d22-4179-443c-8b6e-619a5d662077','ACC-EFD-0818171016','ACC-KH-82533676','user2','UPI','RISK_APPROVED','APPROVED','TRANSFER'),(5.00,'IN',30.00,6,'2026-05-26 11:45:51.535562','2026-05-26 11:44:07.226000','2026-05-26 11:45:51.550546',_binary '\ÍÖëd\ƒH\È∞\r¡*’êHF','0b2410eb-f4e4-4ca1-bf9c-c1a01717d915','DEV-ANDROID-MOBILE','user2@gmail.com','6a8332f6-3fe3-49f8-b4e2-c352b097a795','ACC-EFD-0818171016','ACC-KH-82533676','user2','UPI','RISK_APPROVED','APPROVED','TRANSFER'),(5.00,'IN',10.00,2,'2026-05-26 11:45:51.384958','2026-05-26 11:44:07.226000','2026-05-26 11:45:51.407506',_binary '\ÍÖëd\ƒH\È∞\r¡*’êHF','0e366729-175c-4827-a3f9-2cc1675e14ed','DEV-ANDROID-MOBILE','user2@gmail.com','4b9e5b8e-c0aa-4206-829d-b815c1606fba','ACC-EFD-0818171016','ACC-KH-82533676','user2','UPI','RISK_APPROVED','APPROVED','TRANSFER'),(5.00,'IN',40.00,8,'2026-05-26 11:45:52.057214','2026-05-26 11:44:07.226000','2026-05-26 11:45:52.076668',_binary '\ÍÖëd\ƒH\È∞\r¡*’êHF','53394db6-340c-4fad-88d5-374450708f07','DEV-ANDROID-MOBILE','user2@gmail.com','ebceeacc-b4eb-4938-828d-7b04b8a22e3b','ACC-EFD-0818171016','ACC-KH-82533676','user2','UPI','RISK_APPROVED','APPROVED','TRANSFER'),(5.00,'IN',45.00,9,'2026-05-26 11:45:52.096907','2026-05-26 11:44:07.226000','2026-05-26 11:45:52.112688',_binary '\ÍÖëd\ƒH\È∞\r¡*’êHF','68867492-c783-4258-8337-256ea9d99988','DEV-ANDROID-MOBILE','user2@gmail.com','0bb11fd6-74ba-4bad-b2fc-d62fab339d1d','ACC-EFD-0818171016','ACC-KH-82533676','user2','UPI','RISK_APPROVED','APPROVED','TRANSFER'),(5.00,'IN',35.00,7,'2026-05-26 11:45:52.015568','2026-05-26 11:44:07.226000','2026-05-26 11:45:52.033061',_binary '\ÍÖëd\ƒH\È∞\r¡*’êHF','7c1af14c-73d4-40fd-b193-b3a4635ed4c3','DEV-ANDROID-MOBILE','user2@gmail.com','efc20ffe-7700-44d7-842e-9de45c757d87','ACC-EFD-0818171016','ACC-KH-82533676','user2','UPI','RISK_APPROVED','APPROVED','TRANSFER'),(5.00,'IN',5.00,1,'2026-05-26 11:45:50.952663','2026-05-26 11:44:07.226000','2026-05-26 11:45:51.343367',_binary '\ÍÖëd\ƒH\È∞\r¡*’êHF','8bf60512-1610-4b46-8409-e6f63bc012e4','DEV-ANDROID-MOBILE','user2@gmail.com','014e23cd-6643-40b1-8934-bcfd4f735ed3','ACC-EFD-0818171016','ACC-KH-82533676','user2','UPI','RISK_APPROVED','APPROVED','TRANSFER'),(5.00,'IN',25.00,5,'2026-05-26 11:45:51.498458','2026-05-26 11:44:07.226000','2026-05-26 11:45:51.514405',_binary '\ÍÖëd\ƒH\È∞\r¡*’êHF','e6ed8db0-530a-43e2-bf53-a49fbe0645e2','DEV-ANDROID-MOBILE','user2@gmail.com','b493c653-5983-4ac2-8fef-b025accb6326','ACC-EFD-0818171016','ACC-KH-82533676','user2','UPI','RISK_APPROVED','APPROVED','TRANSFER'),(5.00,'IN',20.00,4,'2026-05-26 11:45:51.464062','2026-05-26 11:44:07.226000','2026-05-26 11:45:51.477959',_binary '\ÍÖëd\ƒH\È∞\r¡*’êHF','e91f49db-c8d7-4a13-b48c-34c2254f269c','DEV-ANDROID-MOBILE','user2@gmail.com','e3e36392-3e4b-41fd-93d7-90014fdcb9e1','ACC-EFD-0818171016','ACC-KH-82533676','user2','UPI','RISK_APPROVED','APPROVED','TRANSFER');
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `is_active` tinyint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `phone_number` varchar(255) NOT NULL,
  `user_name` varchar(255) NOT NULL,
  `role` enum('ADMIN','USER') NOT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `users_chk_1` CHECK ((`is_active` between 0 and 3))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (0,'2026-01-29 08:45:42.364508',1,'admin@gmail.com','$2a$10$rEf4w/Q2qPkb2NkL453KkOfUFxpVaQTn/LygCu1zOGJrD4tE0GHj6','9999999999','admin','ADMIN'),(0,'2026-01-29 08:51:05.848156',2,'test01@gmail.com','$2a$10$NRIg75kD/5/owfg8XP8g4.X/fC7w8x07sw0ycJWr.3xJXmekbJDQ2','9888888888','test01','USER');
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

-- Dump completed on 2026-05-27 14:58:59
