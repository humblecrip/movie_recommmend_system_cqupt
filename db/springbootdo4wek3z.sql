-- MySQL dump 10.13  Distrib 5.7.31, for Linux (x86_64)
--
-- Host: localhost    Database: springbootdo4wek3z
-- ------------------------------------------------------
-- Server version	5.7.31

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `springbootdo4wek3z`
--

/*!40000 DROP DATABASE IF EXISTS `springbootdo4wek3z`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `springbootdo4wek3z` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `springbootdo4wek3z`;

--
-- Table structure for table `config`
--

DROP TABLE IF EXISTS `config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '配置参数名称',
  `value` varchar(100) DEFAULT NULL COMMENT '配置参数值',
  `url` varchar(500) DEFAULT NULL COMMENT 'url',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COMMENT='配置文件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `config`
--

LOCK TABLES `config` WRITE;
/*!40000 ALTER TABLE `config` DISABLE KEYS */;
INSERT INTO `config` VALUES (1,'picture1','upload/picture1.jpg',NULL),(2,'picture2','upload/picture2.jpg',NULL),(3,'picture3','upload/picture3.jpg',NULL);
/*!40000 ALTER TABLE `config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dianyingleixing`
--

DROP TABLE IF EXISTS `dianyingleixing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dianyingleixing` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `dianyingleixing` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电影类型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电影类型';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dianyingleixing`
--

LOCK TABLES `dianyingleixing` WRITE;
/*!40000 ALTER TABLE `dianyingleixing` DISABLE KEYS */;
INSERT INTO `dianyingleixing` VALUES (1,'2025-04-12 12:01:22','电影类型1'),(2,'2025-04-12 12:01:22','电影类型2'),(3,'2025-04-12 12:01:22','电影类型3'),(4,'2025-04-12 12:01:22','电影类型4'),(5,'2025-04-12 12:01:22','电影类型5'),(6,'2025-04-12 12:01:22','电影类型6'),(7,'2025-04-12 12:01:22','电影类型7'),(8,'2025-04-12 12:01:22','电影类型8'),(9,'2025-04-12 12:16:54','动作');
/*!40000 ALTER TABLE `dianyingleixing` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dianyingxinxi`
--

DROP TABLE IF EXISTS `dianyingxinxi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dianyingxinxi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `dianyingmingcheng` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电影名称',
  `haibao` longtext COLLATE utf8mb4_unicode_ci COMMENT '海报',
  `dianyingleixing` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电影类型',
  `quyu` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区域',
  `shangyingshijian` date DEFAULT NULL COMMENT '上映时间',
  `daoyan` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '导演',
  `zhuyan` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主演',
  `juqingjianjie` longtext COLLATE utf8mb4_unicode_ci COMMENT '剧情简介',
  `dianyingxiangqing` longtext COLLATE utf8mb4_unicode_ci COMMENT '电影详情',
  `thumbsupnum` int(11) DEFAULT '0' COMMENT '赞',
  `crazilynum` int(11) DEFAULT '0' COMMENT '踩',
  `clicktime` datetime DEFAULT NULL COMMENT '最近点击时间',
  `clicknum` int(11) DEFAULT '0' COMMENT '点击次数',
  `discussnum` int(11) DEFAULT '0' COMMENT '评论数',
  `totalscore` double DEFAULT '0' COMMENT '评分',
  `storeupnum` int(11) DEFAULT '0' COMMENT '收藏数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电影信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dianyingxinxi`
--

LOCK TABLES `dianyingxinxi` WRITE;
/*!40000 ALTER TABLE `dianyingxinxi` DISABLE KEYS */;
INSERT INTO `dianyingxinxi` VALUES (1,'2025-04-12 12:01:22','电影名称1','upload/dianyingxinxi_haibao1.jpg,upload/dianyingxinxi_haibao2.jpg,upload/dianyingxinxi_haibao3.jpg','电影类型1','大陆','2025-04-12','导演1','主演1','剧情简介1','电影详情1',1,1,'2025-04-12 20:01:22',2,0,0,1),(2,'2025-04-12 12:01:22','电影名称2','upload/dianyingxinxi_haibao2.jpg,upload/dianyingxinxi_haibao3.jpg,upload/dianyingxinxi_haibao4.jpg','电影类型2','大陆','2025-04-12','导演2','主演2','剧情简介2','电影详情2',3,2,'2025-04-12 20:01:22',4,0,0,3),(3,'2025-04-12 12:01:22','电影名称3','upload/dianyingxinxi_haibao3.jpg,upload/dianyingxinxi_haibao4.jpg,upload/dianyingxinxi_haibao5.jpg','电影类型3','大陆','2025-04-12','导演3','主演3','剧情简介3','电影详情3',3,3,'2025-04-12 20:01:22',3,0,0,3),(4,'2025-04-12 12:01:22','电影名称4','upload/dianyingxinxi_haibao4.jpg,upload/dianyingxinxi_haibao5.jpg,upload/dianyingxinxi_haibao6.jpg','电影类型4','大陆','2025-04-12','导演4','主演4','剧情简介4','电影详情4',4,4,'2025-04-12 20:01:22',4,0,0,4),(5,'2025-04-12 12:01:22','电影名称5','upload/dianyingxinxi_haibao5.jpg,upload/dianyingxinxi_haibao6.jpg,upload/dianyingxinxi_haibao7.jpg','电影类型5','大陆','2025-04-12','导演5','主演5','剧情简介5','电影详情5',5,5,'2025-04-12 20:01:22',5,0,0,5),(6,'2025-04-12 12:01:22','电影名称6','upload/dianyingxinxi_haibao6.jpg,upload/dianyingxinxi_haibao7.jpg,upload/dianyingxinxi_haibao8.jpg','电影类型6','大陆','2025-04-12','导演6','主演6','剧情简介6','电影详情6',6,6,'2025-04-12 20:01:22',6,0,0,6),(7,'2025-04-12 12:01:22','电影名称7','upload/dianyingxinxi_haibao7.jpg,upload/dianyingxinxi_haibao8.jpg,upload/dianyingxinxi_haibao1.jpg','电影类型7','大陆','2025-04-12','导演7','主演7','剧情简介7','电影详情7',9,7,'2025-04-12 20:01:22',9,2,2.5,9),(8,'2025-04-12 12:01:22','电影名称8','upload/dianyingxinxi_haibao8.jpg,upload/dianyingxinxi_haibao1.jpg,upload/dianyingxinxi_haibao2.jpg','电影类型8','大陆','2025-04-12','导演8','主演8','剧情简介8','电影详情8',8,8,'2025-04-12 20:01:22',9,0,0,8),(9,'2025-04-12 12:17:26','手动阀','upload/1744460226960.png','动作','韩国','2025-04-02','魂牵','李大','没有哪件事，不动手就可以实现。只要你愿意走，哪里都会有路。看不到美好，是因为你没有坚持走下去，人生贵在行动，迟疑不决时，不妨先迈出一小步。','<p>你勤奋充电，你努力工作，你保持身材，你对人微笑，这些都不是为了取悦他人，而是为了扮靓自己，照亮自己的心，告诉自己：我是一股独立向上的力量。</p>',1,0,NULL,3,0,1,2);
/*!40000 ALTER TABLE `dianyingxinxi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `discussdianyingxinxi`
--

DROP TABLE IF EXISTS `discussdianyingxinxi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `discussdianyingxinxi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `refid` bigint(20) NOT NULL COMMENT '关联表id',
  `userid` bigint(20) NOT NULL COMMENT '用户id',
  `avatarurl` longtext COLLATE utf8mb4_unicode_ci COMMENT '头像',
  `nickname` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
  `content` longtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `score` double DEFAULT NULL COMMENT '评分',
  `reply` longtext COLLATE utf8mb4_unicode_ci COMMENT '回复内容',
  `thumbsupnum` int(11) DEFAULT '0' COMMENT '赞',
  `crazilynum` int(11) DEFAULT '0' COMMENT '踩',
  `istop` int(11) DEFAULT '0' COMMENT '置顶(1:置顶,0:非置顶)',
  `tuserids` longtext COLLATE utf8mb4_unicode_ci COMMENT '赞用户ids',
  `cuserids` longtext COLLATE utf8mb4_unicode_ci COMMENT '踩用户ids',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电影信息评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `discussdianyingxinxi`
--

LOCK TABLES `discussdianyingxinxi` WRITE;
/*!40000 ALTER TABLE `discussdianyingxinxi` DISABLE KEYS */;
INSERT INTO `discussdianyingxinxi` VALUES (1,'2025-04-12 12:15:37',7,1744460114869,'upload/1744460111828.png','11','<p>666</p>',4,NULL,0,0,0,NULL,NULL),(2,'2025-04-12 12:16:08',7,12,'upload/yonghu_touxiang2.jpg','用户账号2','<p>**,**,**,**,**,**,**,**,**,**,词与词之间英文逗号隔开</p>',1,NULL,0,0,0,NULL,NULL);
/*!40000 ALTER TABLE `discussdianyingxinxi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sensitivewords`
--

DROP TABLE IF EXISTS `sensitivewords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sensitivewords` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `content` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '敏感词：垃圾,黄色网页,毒品,台独,港独,藏独,王八蛋,海洛因,白粉,傻逼' COMMENT '内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sensitivewords`
--

LOCK TABLES `sensitivewords` WRITE;
/*!40000 ALTER TABLE `sensitivewords` DISABLE KEYS */;
INSERT INTO `sensitivewords` VALUES (1,'2025-04-12 12:01:22','敏感词：垃圾,黄色网页,毒品,台独,港独,藏独,王八蛋,海洛因,白粉,傻逼,AA');
/*!40000 ALTER TABLE `sensitivewords` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storeup`
--

DROP TABLE IF EXISTS `storeup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storeup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `userid` bigint(20) NOT NULL COMMENT '用户id',
  `refid` bigint(20) DEFAULT NULL COMMENT '商品id',
  `tablename` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '表名',
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '名称',
  `picture` longtext COLLATE utf8mb4_unicode_ci COMMENT '图片',
  `type` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '1' COMMENT '类型',
  `inteltype` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推荐类型',
  `remark` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storeup`
--

LOCK TABLES `storeup` WRITE;
/*!40000 ALTER TABLE `storeup` DISABLE KEYS */;
INSERT INTO `storeup` VALUES (1,'2025-04-12 12:15:24',1744460114869,2,'dianyingxinxi','电影名称2','upload/dianyingxinxi_haibao2.jpg','1',NULL,NULL),(2,'2025-04-12 12:15:26',1744460114869,2,'dianyingxinxi','电影名称2','upload/dianyingxinxi_haibao2.jpg','21',NULL,NULL),(3,'2025-04-12 12:15:32',1744460114869,7,'dianyingxinxi','电影名称7','upload/dianyingxinxi_haibao7.jpg','1',NULL,NULL),(4,'2025-04-12 12:15:34',1744460114869,7,'dianyingxinxi','电影名称7','upload/dianyingxinxi_haibao7.jpg','21',NULL,NULL),(5,'2025-04-12 12:15:56',12,7,'dianyingxinxi','电影名称7','upload/dianyingxinxi_haibao7.jpg','1',NULL,NULL),(6,'2025-04-12 12:15:59',12,7,'dianyingxinxi','电影名称7','upload/dianyingxinxi_haibao7.jpg','21',NULL,NULL),(7,'2025-04-12 12:17:55',1744460114869,9,'dianyingxinxi','手动阀','upload/1744460226960.png','1',NULL,NULL),(8,'2025-04-12 12:17:57',1744460114869,9,'dianyingxinxi','手动阀','upload/1744460226960.png','21',NULL,NULL),(9,'2025-04-12 12:19:24',17,9,'dianyingxinxi','手动阀','upload/1744460226960.png','1',NULL,NULL);
/*!40000 ALTER TABLE `storeup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `token`
--

DROP TABLE IF EXISTS `token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userid` bigint(20) NOT NULL COMMENT '用户id',
  `username` varchar(100) NOT NULL COMMENT '用户名',
  `tablename` varchar(100) DEFAULT NULL COMMENT '表名',
  `role` varchar(100) DEFAULT NULL COMMENT '角色',
  `token` varchar(200) NOT NULL COMMENT '密码',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `expiratedtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '过期时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='token表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `token`
--

LOCK TABLES `token` WRITE;
/*!40000 ALTER TABLE `token` DISABLE KEYS */;
INSERT INTO `token` VALUES (1,1744460114869,'11','yonghu','用户','c92mzgxgl4joajebqqrvef2em9vi06vf','2025-04-12 12:15:17','2025-04-12 13:17:51'),(2,12,'用户账号2','yonghu','用户','k36tk5mfwn2a17i2wkh9lswbss26p3vh','2025-04-12 12:15:50','2025-04-12 13:15:51'),(3,1,'admin','users','管理员','o1ah05w1hdx2uzhyfa0qphk8hrmn3y6q','2025-04-12 12:16:39','2025-04-12 13:18:31'),(4,17,'用户账号7','yonghu','用户','sp183x7fux7apmphhnpdx70rzlmklbxw','2025-04-12 12:19:13','2025-04-12 13:19:13');
/*!40000 ALTER TABLE `token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(100) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `image` varchar(200) DEFAULT NULL COMMENT '头像',
  `role` varchar(100) DEFAULT '管理员' COMMENT '角色',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='管理员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','admin','upload/image1.jpg','管理员','2025-04-12 12:01:22');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yonghu`
--

DROP TABLE IF EXISTS `yonghu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yonghu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `yonghuzhanghao` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户账号',
  `mima` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `yonghuxingming` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户姓名',
  `touxiang` longtext COLLATE utf8mb4_unicode_ci COMMENT '头像',
  `xingbie` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '性别',
  `lianxidianhua` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `shenfenzheng` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '身份证',
  PRIMARY KEY (`id`),
  UNIQUE KEY `yonghuzhanghao` (`yonghuzhanghao`)
) ENGINE=InnoDB AUTO_INCREMENT=1744460114870 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yonghu`
--

LOCK TABLES `yonghu` WRITE;
/*!40000 ALTER TABLE `yonghu` DISABLE KEYS */;
INSERT INTO `yonghu` VALUES (11,'2025-04-12 12:01:22','用户账号1','123456','用户姓名1','upload/yonghu_touxiang1.jpg','男','13823888881','440300199101010001'),(12,'2025-04-12 12:01:22','用户账号2','123456','用户姓名2','upload/yonghu_touxiang2.jpg','男','13823888882','440300199202020002'),(13,'2025-04-12 12:01:22','用户账号3','123456','用户姓名3','upload/yonghu_touxiang3.jpg','男','13823888883','440300199303030003'),(14,'2025-04-12 12:01:22','用户账号4','123456','用户姓名4','upload/yonghu_touxiang4.jpg','男','13823888884','440300199404040004'),(15,'2025-04-12 12:01:22','用户账号5','123456','用户姓名5','upload/yonghu_touxiang5.jpg','男','13823888885','440300199505050005'),(16,'2025-04-12 12:01:22','用户账号6','123456','用户姓名6','upload/yonghu_touxiang6.jpg','男','13823888886','440300199606060006'),(17,'2025-04-12 12:01:22','用户账号7','123456','用户姓名7','upload/yonghu_touxiang7.jpg','男','13823888887','440300199707070007'),(18,'2025-04-12 12:01:22','用户账号8','123456','用户姓名8','upload/yonghu_touxiang8.jpg','男','13823888888','440300199808080008'),(1744460114869,'2025-04-12 12:15:14','11','11','王子','upload/1744460111828.png','男','13509091122','441400200012132542');
/*!40000 ALTER TABLE `yonghu` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-16 11:22:09
