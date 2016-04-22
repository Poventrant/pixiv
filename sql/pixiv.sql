
DROP DATABASE IF EXISTS pixiv;
CREATE DATABASE pixiv;

SET FOREIGN_KEY_CHECKS=0;
USE pixiv;
-- ----------------------------
-- Table structure for ar_pixiv
-- ----------------------------
DROP TABLE IF EXISTS `pixiv`;
CREATE TABLE `pixiv` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `size` varchar(100) DEFAULT NULL,
  `master_path` varchar(255) DEFAULT NULL,
  `save_path` varchar(255) DEFAULT NULL,
  `create_time` date DEFAULT NULL,
  `rate` double DEFAULT NULL,
  `isdelete` bit(1) DEFAULT NULL,
  `origin_href` varchar(255) DEFAULT NULL,
  `author` varchar(255) DEFAULT NULL,
  `picid` varchar(50) DEFAULT NULL,
  `picno` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `picid` (`picid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
