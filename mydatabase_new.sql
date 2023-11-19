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
  `zoned_date_time_locked_user` datetime DEFAULT NULL,
  `user_details_id` bigint DEFAULT NULL,
  PRIMARY KEY (`admin_id`),
  KEY `FKsx6m93kltrhxqb2m03r6p8ilt` (`user_details_id`),
  CONSTRAINT `FKsx6m93kltrhxqb2m03r6p8ilt` FOREIGN KEY (`user_details_id`) REFERENCES `user_details` (`user_details_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'amineamine1234@email.com',_binary '',_binary '',_binary '',_binary '\0',_binary '','$2a$10$IvunFNgxnmoPloCEOzRwje.bGG5015cBq8McHKjZLuRu5MyWTP8O.','superAdmin',NULL,NULL),(2,'generetedASuperAdmin@email.com',_binary '',_binary '',_binary '',_binary '\0',_binary '','$2a$10$LIWHUensM/rQASQDWJlFsuAzhdYyqWlryGkvL/tYs7/pj0tzUErRO','generetedSuperAdmin',NULL,NULL);
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
INSERT INTO `admin_permissions` VALUES (1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,10),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),(1,18),(1,19),(1,20),(1,21);
/*!40000 ALTER TABLE `admin_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `blacklist_entry`
--

DROP TABLE IF EXISTS `blacklist_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `blacklist_entry` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expiry_time` datetime DEFAULT NULL,
  `token` text,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `blacklist_entry`
--

LOCK TABLES `blacklist_entry` WRITE;
/*!40000 ALTER TABLE `blacklist_entry` DISABLE KEYS */;
/*!40000 ALTER TABLE `blacklist_entry` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
INSERT INTO `permission` VALUES (1,'SUPER_ADMIN'),(2,'write:user'),(3,'read:user'),(4,'read:manager'),(5,'write:manager'),(6,'write:admin'),(7,'read:admin'),(8,'disable:user'),(9,'enable:user'),(10,'write:project'),(11,'read:project'),(12,'write:subtask'),(13,'read:subtask'),(14,'write:task'),(15,'read:task'),(16,'disable:manager_role'),(17,'enable:manager_role'),(18,'affect:role_permission'),(19,'write:super_admin'),(20,'read:super_admin'),(21,'affect:users_to_project'),(22,'ROLE_ADMIN'),(23,'ROLE_MANAGER');
/*!40000 ALTER TABLE `permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_user`
--

DROP TABLE IF EXISTS `project_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_user` (
  `user_id` bigint NOT NULL,
  `project_manager_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`project_manager_id`),
  KEY `FK9c2ef81i0grxub5f0ryr6hyxs` (`project_manager_id`),
  CONSTRAINT `FK9c2ef81i0grxub5f0ryr6hyxs` FOREIGN KEY (`project_manager_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `FKel8m9xoq58jhek2k46cnindbf` FOREIGN KEY (`user_id`) REFERENCES `projects_of_manager` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_user`
--

LOCK TABLES `project_user` WRITE;
/*!40000 ALTER TABLE `project_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_user` ENABLE KEYS */;
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
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`project_id`),
  KEY `FKb2r9vtf7kg6dg01hf8uacxktf` (`user_id`),
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
-- Table structure for table `projects_of_manager`
--

DROP TABLE IF EXISTS `projects_of_manager`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `projects_of_manager` (
  `project_id` bigint NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `department` varchar(255) DEFAULT NULL,
  `description` longtext,
  `end_project` datetime DEFAULT NULL,
  `name_project` varchar(255) DEFAULT NULL,
  `priority` int DEFAULT NULL,
  `project_start` datetime DEFAULT NULL,
  `manager_id` bigint DEFAULT NULL,
  PRIMARY KEY (`project_id`),
  KEY `FKohjv3t4q5mdemx64w2qo77ji6` (`manager_id`),
  CONSTRAINT `FKohjv3t4q5mdemx64w2qo77ji6` FOREIGN KEY (`manager_id`) REFERENCES `admin` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `projects_of_manager`
--

LOCK TABLES `projects_of_manager` WRITE;
/*!40000 ALTER TABLE `projects_of_manager` DISABLE KEYS */;
/*!40000 ALTER TABLE `projects_of_manager` ENABLE KEYS */;
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
-- Table structure for table `task_manager_admin_historic`
--

DROP TABLE IF EXISTS `task_manager_admin_historic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_manager_admin_historic` (
  `admin_id` bigint NOT NULL AUTO_INCREMENT,
  `error_message` varchar(255) DEFAULT NULL,
  `is_success_operation` bit(1) NOT NULL,
  `operation` varchar(255) DEFAULT NULL,
  `response_body` varchar(255) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_manager_admin_historic`
--

LOCK TABLES `task_manager_admin_historic` WRITE;
/*!40000 ALTER TABLE `task_manager_admin_historic` DISABLE KEYS */;
INSERT INTO `task_manager_admin_historic` VALUES (1,NULL,_binary '','REGISTER_SUPER_ADMIN','Super Admin with userName superAdmin was registered','2023-10-25 17:24:21','superAdmin'),(2,NULL,_binary '','LOGIN','Successful created jwt','2023-10-25 17:24:32','superAdmin');
/*!40000 ALTER TABLE `task_manager_admin_historic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_manager_user_historic`
--

DROP TABLE IF EXISTS `task_manager_user_historic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_manager_user_historic` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `error_message` varchar(255) DEFAULT NULL,
  `is_success_operation` bit(1) NOT NULL,
  `operation` varchar(255) DEFAULT NULL,
  `response_body` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_manager_user_historic`
--

LOCK TABLES `task_manager_user_historic` WRITE;
/*!40000 ALTER TABLE `task_manager_user_historic` DISABLE KEYS */;
/*!40000 ALTER TABLE `task_manager_user_historic` ENABLE KEYS */;
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
  `project_manager_id` bigint DEFAULT NULL,
  PRIMARY KEY (`task_id`),
  KEY `FKsfhn82y57i3k9uxww1s007acc` (`project_id`),
  KEY `FKk5til3uurh7mnwlu8i1ii9br3` (`project_manager_id`),
  CONSTRAINT `FKk5til3uurh7mnwlu8i1ii9br3` FOREIGN KEY (`project_manager_id`) REFERENCES `projects_of_manager` (`project_id`),
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
  `email` varchar(255) DEFAULT NULL,
  `is_account_non_expired` bit(1) NOT NULL,
  `is_account_non_locked` bit(1) NOT NULL,
  `is_credentials_non_expired` bit(1) NOT NULL,
  `is_enabled` bit(1) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `zoned_date_time_locked_user` datetime DEFAULT NULL,
  `admin_id` bigint DEFAULT NULL,
  `user_details_id` bigint DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  KEY `FKpi0nc9w2flbfset55lce6a63f` (`admin_id`),
  KEY `FK3wsl4duq3n5imh005r68f3uar` (`user_details_id`),
  CONSTRAINT `FK3wsl4duq3n5imh005r68f3uar` FOREIGN KEY (`user_details_id`) REFERENCES `user_details` (`user_details_id`),
  CONSTRAINT `FKpi0nc9w2flbfset55lce6a63f` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_details`
--

DROP TABLE IF EXISTS `user_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_details` (
  `user_details_id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `birthday` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `image_path` varchar(255) DEFAULT NULL,
  `land` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_details_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_details`
--

LOCK TABLES `user_details` WRITE;
/*!40000 ALTER TABLE `user_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_details` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-10-25 15:28:18
