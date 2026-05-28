-- MySQL dump 10.13  Distrib 9.4.0, for macos26.0 (arm64)
--
-- Host: localhost    Database: gbank_transaction-observability-service
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
  PRIMARY KEY (`id`),
  KEY `idx_failed_at` (`failed_at`),
  KEY `idx_transaction_id` (`transaction_id`)
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
-- Table structure for table `processed_events`
--

DROP TABLE IF EXISTS `processed_events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `processed_events` (
  `processed_at` datetime(6) NOT NULL,
  `event_id` binary(16) NOT NULL,
  PRIMARY KEY (`event_id`),
  KEY `idx_processed_at` (`processed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `processed_events`
--

LOCK TABLES `processed_events` WRITE;
/*!40000 ALTER TABLE `processed_events` DISABLE KEYS */;
INSERT INTO `processed_events` VALUES ('2026-05-26 11:45:52.828476',_binary '\Z\ÎÿäMjF æƒù®˘m'),('2026-05-26 11:45:52.828476',_binary 'û?¥êQ\ﬂN)§5®hé\‚ß'),('2026-05-26 11:45:52.860177',_binary 's›¥êKHDò#Å]i\ÔK˛'),('2026-05-26 11:45:52.869609',_binary '{oæ\ÂÜM	É¨R\ÁØU&K'),('2026-05-26 11:45:52.888473',_binary '\≈ﬂ∏•ìO´ªA\Ëi-`'),('2026-05-26 11:45:52.902874',_binary 'º1\Ùu\ÔwMó\Â\«˛û\ﬂ\≈'),('2026-05-26 11:45:52.915512',_binary '=¸ W\«ADÇI\–m\ÃC•'),('2026-05-26 11:45:52.926326',_binary 'ê|\‹S\€O“¶\—˚\›\Ò\‚≥'),('2026-05-26 11:45:52.936274',_binary 'Âôí\È\ ;F7íW\ı\ÏR\ÿ\—U');
/*!40000 ALTER TABLE `processed_events` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transaction_event_records`
--

DROP TABLE IF EXISTS `transaction_event_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transaction_event_records` (
  `event_version` int NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `occurred_at` datetime(6) NOT NULL,
  `recorded_at` datetime(6) NOT NULL,
  `event_id` binary(16) NOT NULL,
  `transaction_id` binary(16) NOT NULL,
  `checksum` varchar(64) NOT NULL,
  `event_type` varchar(255) NOT NULL,
  `source_service` varchar(255) NOT NULL,
  `payload` text NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_id` (`event_id`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_transaction_id` (`transaction_id`),
  KEY `idx_recorded_at` (`recorded_at`),
  KEY `idx_occurred_at` (`occurred_at`),
  KEY `idx_recorded_at_transaction_id` (`recorded_at`,`transaction_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction_event_records`
--

LOCK TABLES `transaction_event_records` WRITE;
/*!40000 ALTER TABLE `transaction_event_records` DISABLE KEYS */;
INSERT INTO `transaction_event_records` VALUES (1,1,'2026-05-26 11:45:51.344005','2026-05-26 11:45:52.790010',_binary 'û?¥êQ\ﬂN)§5®hé\‚ß',_binary 'ã\ˆKFÑ	\Ê\ˆ;¿\‰','8a58aeb183c6af8dcc58cd33b4d5b0c432ababaaa90e191beaea933c366ac550','TRANSACTION_FINALIZED','transaction-service','{\"eventId\":\"9e3fb490-51df-4e29-a435-a8688ee2a70f\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:51.344005Z\",\"transactionId\":\"8bf60512-1610-4b46-8409-e6f63bc012e4\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:fefd35cb-0b21-4573-a0b4-f1156072f4a6\"}'),(1,2,'2026-05-26 11:45:51.407636','2026-05-26 11:45:52.790009',_binary '\Z\ÎÿäMjF æƒù®˘m',_binary '6g)\\H\'£˘,¡g^\Ì','0e9b4f7ed46692f1a7566c8618e418dc49b1fd2f441bae92c3f3d295af2a5297','TRANSACTION_FINALIZED','transaction-service','{\"eventId\":\"1aebd88a-4d6a-4620-be04-c49d16a8f96d\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:51.407636Z\",\"transactionId\":\"0e366729-175c-4827-a3f9-2cc1675e14ed\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:2a63a4b5-4d76-40f3-9c89-da0217943997\"}'),(1,3,'2026-05-26 11:45:51.443420','2026-05-26 11:45:52.858749',_binary 's›¥êKHDò#Å]i\ÔK˛',_binary '6\IKL\rÇY`≤~Z%Ü','e2e857cc7dc6fdc0aef781ad3d9fc159e5045bf012c8d9fdb76d1f34afe9ca8c','TRANSACTION_FINALIZED','transaction-service','{\"eventId\":\"73ddb490-4b48-4403-9823-815d69ef4bfe\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:51.443420Z\",\"transactionId\":\"0b1d36f0-494b-4c0d-8259-60b27e5a2586\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:b5f2491a-07db-4932-aa25-d91019069d27\"}'),(1,4,'2026-05-26 11:45:51.478102','2026-05-26 11:45:52.867010',_binary '{oæ\ÂÜM	É¨R\ÁØU&K',_binary '\ÈI\€\»\◊J¥å4\¬%O&ú','75e9c2ce926d6e2ce1c1254a002ccb387891eed5fbd00925a1bdbade8a3039de','TRANSACTION_FINALIZED','transaction-service','{\"eventId\":\"057b6fbe-e586-4d09-83ac-52e7af55264b\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:51.478102Z\",\"transactionId\":\"e91f49db-c8d7-4a13-b48c-34c2254f269c\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:97da2325-234c-4dc8-abc1-7855a436a22c\"}'),(1,5,'2026-05-26 11:45:51.514543','2026-05-26 11:45:52.886582',_binary '\≈ﬂ∏•ìO´ªA\Ëi-`',_binary '\ÊÌç∞S\nC\‚øS§üæE\‚','058a40f57f51e898e7570273e3d9099bbe1a26ab70f2add71875586daefbd39c','TRANSACTION_FINALIZED','transaction-service','{\"eventId\":\"c5dfb8a5-1d93-4fab-bb41-e8691e2d601f\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:51.514543Z\",\"transactionId\":\"e6ed8db0-530a-43e2-bf53-a49fbe0645e2\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:7215e82a-8dab-49e8-963d-33e71c6277d6\"}'),(1,6,'2026-05-26 11:45:51.550696','2026-05-26 11:45:52.901426',_binary 'º1\Ùu\ÔwMó\Â\«˛û\ﬂ\≈',_binary '$\Î\Ù\‰L°øú¡†\Ÿ','e755f2ecec41451d4a03cdeb9e0fd9fde8ed849694a80fd345d4a334c3b14fda','TRANSACTION_FINALIZED','transaction-service','{\"eventId\":\"bc31f475-ef77-4d06-97e5-c7fe9edf14c5\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:51.550696Z\",\"transactionId\":\"0b2410eb-f4e4-4ca1-bf9c-c1a01717d915\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:4aa563d3-fce2-4478-a24d-6182cf274297\"}'),(1,7,'2026-05-26 11:45:52.033188','2026-05-26 11:45:52.914280',_binary '=¸ W\«ADÇI\–m\ÃC•',_binary '|\Z\ÒLs\‘@˝±ì≥§c^\‘\√','c732235eb825509be64c4764267705f6e1bf01e0e9c3e0a3dba3fd37d701a57c','TRANSACTION_FINALIZED','transaction-service','{\"eventId\":\"3d18fc20-57c7-4144-8214-49d06dcc43a5\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:52.033188Z\",\"transactionId\":\"7c1af14c-73d4-40fd-b193-b3a4635ed4c3\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:473c7e0f-33d5-44f0-82ce-9c1e35701be1\"}'),(1,8,'2026-05-26 11:45:52.076877','2026-05-26 11:45:52.925222',_binary 'ê|\‹S\€O“¶\—˚\›\Ò\‚≥',_binary 'S9M∂4O≠à\’7DPpè','2bb20768415a2a273efa8a8a7c43050361adca6f08316afe36c858951a1e5f5c','TRANSACTION_FINALIZED','transaction-service','{\"eventId\":\"907c18dc-53db-4fd2-a61d-d1fbddf1e2b3\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:52.076877Z\",\"transactionId\":\"53394db6-340c-4fad-88d5-374450708f07\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:5de96ac3-c9f1-4f4c-a409-d630bde23d55\"}'),(1,9,'2026-05-26 11:45:52.112797','2026-05-26 11:45:52.935155',_binary 'Âôí\È\ ;F7íW\ı\ÏR\ÿ\—U',_binary 'hÜtí«ÉBXÉ7%n©Ÿôà','0fb2522ad04c0f334793977e2819b420bc53f520bcba9f24727cd5738395a71a','TRANSACTION_FINALIZED','transaction-service','{\"eventId\":\"e59992e9-ca3b-4637-9257-f5ec52d8d155\",\"eventType\":\"TRANSACTION_FINALIZED\",\"eventVersion\":1,\"occurredAt\":\"2026-05-26T11:45:52.112797Z\",\"transactionId\":\"68867492-c783-4258-8337-256ea9d99988\",\"userId\":\"ea078591-64c4-48e9-b00d-c12ad5904846\",\"userName\":\"user2\",\"email\":\"user2@gmail.com\",\"amount\":5,\"finalStatus\":\"APPROVED\",\"correlationId\":\"Co-relationId:529630a2-a0a9-4d33-81e1-9755ea548dda\"}');
/*!40000 ALTER TABLE `transaction_event_records` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-27 14:59:29
