-- MySQL dump 10.13  Distrib 8.1.0, for Linux (x86_64)
--
-- Host: localhost    Database: db
-- ------------------------------------------------------
-- Server version	8.1.0

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
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `admin_id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `is_account_non_expired` bit(1) NOT NULL,
  `is_account_non_locked` bit(1) NOT NULL,
  `is_credentials_non_expired` bit(1) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `is_enabled` bit(1) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_permissions`
--

DROP TABLE IF EXISTS `admin_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_permissions` (
  `admin_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  PRIMARY KEY (`admin_id`,`permission_id`),
  KEY `FKsrnwxm0ofgfyrhn8unpa1abpl` (`permission_id`),
  CONSTRAINT `FKq95cmyhdy7didp1kr03vw73ub` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`),
  CONSTRAINT `FKsrnwxm0ofgfyrhn8unpa1abpl` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permession_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_permissions`
--

LOCK TABLES `admin_permissions` WRITE;
/*!40000 ALTER TABLE `admin_permissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `admin_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permission` (
  `permession_id` bigint NOT NULL AUTO_INCREMENT,
  `permission` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`permession_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
/*!40000 ALTER TABLE `permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `projects`
--

DROP TABLE IF EXISTS `projects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `projects` (
  `project_id` bigint NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `department` varchar(255) DEFAULT NULL,
  `description` longtext,
  `end_project` datetime DEFAULT NULL,
  `name_project` varchar(255) DEFAULT NULL,
  `priority` int DEFAULT NULL,
  `project_start` datetime DEFAULT NULL,
  `manager_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`project_id`),
  KEY `FK39tu8x5dytt1nx6y1yf3j5mro` (`manager_id`),
  KEY `FKb2r9vtf7kg6dg01hf8uacxktf` (`user_id`),
  CONSTRAINT `FK39tu8x5dytt1nx6y1yf3j5mro` FOREIGN KEY (`manager_id`) REFERENCES `admin` (`admin_id`),
  CONSTRAINT `FKb2r9vtf7kg6dg01hf8uacxktf` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `projects`
--

LOCK TABLES `projects` WRITE;
/*!40000 ALTER TABLE `projects` DISABLE KEYS */;
/*!40000 ALTER TABLE `projects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subtasks`
--

DROP TABLE IF EXISTS `subtasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subtasks` (
  `id_sub_task` bigint NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `state` int DEFAULT NULL,
  `sub_task_name` varchar(255) DEFAULT NULL,
  `task_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id_sub_task`),
  KEY `FKsvs126nsj9ohhvwjog5ddp76x` (`task_id`),
  CONSTRAINT `FKsvs126nsj9ohhvwjog5ddp76x` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subtasks`
--

LOCK TABLES `subtasks` WRITE;
/*!40000 ALTER TABLE `subtasks` DISABLE KEYS */;
/*!40000 ALTER TABLE `subtasks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tasks`
--

DROP TABLE IF EXISTS `tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tasks` (
  `task_id` bigint NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `description` longtext,
  `name` varchar(255) DEFAULT NULL,
  `priority` int DEFAULT NULL,
  `state` int DEFAULT NULL,
  `project_id` bigint DEFAULT NULL,
  PRIMARY KEY (`task_id`),
  KEY `FKsfhn82y57i3k9uxww1s007acc` (`project_id`),
  CONSTRAINT `FKsfhn82y57i3k9uxww1s007acc` FOREIGN KEY (`project_id`) REFERENCES `projects` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tasks`
--

LOCK TABLES `tasks` WRITE;
/*!40000 ALTER TABLE `tasks` DISABLE KEYS */;
/*!40000 ALTER TABLE `tasks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `disable_period_in_milliseconds` bigint DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `is_account_non_expired` bit(1) NOT NULL,
  `is_account_non_locked` bit(1) NOT NULL,
  `is_credentials_non_expired` bit(1) NOT NULL,
  `is_enabled` bit(1) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `toggle_date` bigint DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `admin_id` bigint DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  KEY `FKpi0nc9w2flbfset55lce6a63f` (`admin_id`),
  CONSTRAINT `FKpi0nc9w2flbfset55lce6a63f` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,_binary '\0',NULL,'user@email.com',_binary '',_binary '',_binary '',_binary '','$2a$10$l6jJuAI5mtGvaSmlDe19UOJ0Xs3LQz0n3lJptbEMRF9cg1HMzEFX6','ROLE_USER',NULL,'user',NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-10-02 12:02:39
