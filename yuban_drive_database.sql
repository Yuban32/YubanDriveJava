/*
Navicat MySQL Data Transfer
Date: 2023-06-18 14:33:00
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_chunk
-- ----------------------------
DROP TABLE IF EXISTS `t_chunk`;
CREATE TABLE `t_chunk` (
  `c_id` int NOT NULL AUTO_INCREMENT COMMENT '分片id,自增',
  `c_md5` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '分片的md5',
  `c_index` int DEFAULT NULL COMMENT '当前分片的偏移量',
  `c_location` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '当前分片的存储路径',
  PRIMARY KEY (`c_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=116 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_file
-- ----------------------------
DROP TABLE IF EXISTS `t_file`;
CREATE TABLE `t_file` (
  `f_id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `f_md5` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件MD5',
  `f_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件名',
  `f_size` decimal(30,0) DEFAULT NULL COMMENT '文件大小',
  `f_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件类型',
  `f_extension` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件后缀名',
  `f_parent_id` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '父文件夹uuid',
  `f_absolute_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件存储的绝对路径',
  `f_relative_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件存储的相对路径,对应t_folder表的文件夹',
  `f_uploader` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '文件上传者',
  `f_upload_time` datetime DEFAULT NULL COMMENT '文件上传时间',
  `f_status` int DEFAULT '1' COMMENT '文件状态,0是放入回收站,1是正常',
  PRIMARY KEY (`f_id`) USING BTREE,
  KEY `fk_uploader` (`f_uploader`),
  CONSTRAINT `fk_uploader` FOREIGN KEY (`f_uploader`) REFERENCES `t_user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_folder
-- ----------------------------
DROP TABLE IF EXISTS `t_folder`;
CREATE TABLE `t_folder` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '创建文件夹的用户名',
  `folder_relative_path` longtext CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '文件夹的相对路径',
  `folder_uuid` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '文件夹的uuid',
  `folder_name` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '文件夹的名字',
  `parent_folder_uuid` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '父文件夹的uuid',
  `folder_create_time` datetime DEFAULT NULL COMMENT '文件夹的创建日期',
  `folder_status` int DEFAULT '1' COMMENT '文件夹状态,0是放入回收站,1是正常',
  PRIMARY KEY (`id`),
  KEY `fk_userName` (`username`),
  KEY `folder_uuid` (`folder_uuid`),
  CONSTRAINT `fk_userName` FOREIGN KEY (`username`) REFERENCES `t_user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uuid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户uuid',
  `username` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '用户名',
  `password` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '用户密码',
  `avatar` longtext CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '用户头像_base64',
  `email` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '用户邮箱',
  `role` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '用户角色,包含user和admin',
  `status` int NOT NULL DEFAULT '0' COMMENT '用户状态0==正常_1==冻结',
  `created` timestamp NULL DEFAULT NULL COMMENT '账号创建时间',
  `last_login` datetime DEFAULT NULL COMMENT '上次登录时间',
  `deleted` tinyint DEFAULT '1' COMMENT '逻辑删除,1==未删除,0==删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_uuid` (`uuid`),
  UNIQUE KEY `unique_username` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Table structure for t_user_storage_quota
-- ----------------------------
DROP TABLE IF EXISTS `t_user_storage_quota`;
CREATE TABLE `t_user_storage_quota` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uuid` varchar(64) NOT NULL COMMENT '用户ID',
  `total_storage` double NOT NULL DEFAULT '21474836480' COMMENT '总配额,默认20GB,单位Byte',
  `used_storage` double NOT NULL DEFAULT '0' COMMENT '已使用的配额,单位为Byte',
  `deleted` tinyint DEFAULT '1' COMMENT '逻辑删除,1==未删除,0==删除',
  PRIMARY KEY (`id`),
  KEY `fk_uuid` (`uuid`),
  CONSTRAINT `fk_uuid` FOREIGN KEY (`uuid`) REFERENCES `t_user` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;
