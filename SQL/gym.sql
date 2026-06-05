/*
 Navicat Premium Dump SQL

 Source Server         : cvc
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : localhost:3306
 Source Schema         : gym

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 05/05/2026
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '/avatar/default.jpg' COMMENT '头像',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username` (`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

INSERT INTO `admin` VALUES (1, 'admin', '$2b$12$lyU0vuL24AlZ6incu7tLcuD7o5xEkbF6Sk/H8P6Kg7FXBNEYQqRwG', '超级管理员', '/avatar/default.jpg', NOW());

-- ----------------------------
-- Table structure for banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `subtitle` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '副标题',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图片地址',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` int DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

INSERT INTO `banner` VALUES (1, '专业健身空间', '科学训练体系 · 顶级教练团队', '/images/gym7.jpg', 1, 1, NOW());
INSERT INTO `banner` VALUES (2, '精品团体课程', '瑜伽 · 搏击 · 动感单车 · 普拉提', '/images/fitness1.jpg', 2, 1, NOW());
INSERT INTO `banner` VALUES (3, '明星教练团队', '国家认证 · 一对一科学指导', '/images/gym1.jpg', 3, 1, NOW());

-- ----------------------------
-- Table structure for booking
-- ----------------------------
DROP TABLE IF EXISTS `booking`;
CREATE TABLE `booking` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `member_id` int NOT NULL COMMENT '会员ID',
  `course_id` int NOT NULL COMMENT '课程ID',
  `booking_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预约时间',
  `status` int DEFAULT 0 COMMENT '状态 0待确认 1已确认 2已取消 3未支付 4已过期',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_member_id` (`member_id` ASC) USING BTREE,
  INDEX `idx_course_id` (`course_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for card
-- ----------------------------
DROP TABLE IF EXISTS `card`;
CREATE TABLE `card` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `member_id` int NOT NULL COMMENT '会员ID',
  `card_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '卡类型 WEEKLY/MONTHLY/YEARLY/COUNT',
  `price` decimal(10, 2) DEFAULT 0.00 COMMENT '价格',
  `status` int DEFAULT 0 COMMENT '状态 0待审核 1生效中 2已过期 3已退款 4未支付 5已确认支付待审',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_member_id` (`member_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for card_type
-- ----------------------------
DROP TABLE IF EXISTS `card_type`;
CREATE TABLE `card_type` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `card_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '卡名称',
  `card_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '卡标识',
  `price` decimal(10, 2) DEFAULT 0.00 COMMENT '价格',
  `validity_days` int DEFAULT 30 COMMENT '有效期数量',
  `validity_unit` varchar(10) DEFAULT 'DAY' COMMENT '有效期单位 DAY/MONTH/YEAR',
  `cover_image` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '封面图',
  `status` int DEFAULT 1 COMMENT '状态 0禁用 1正常',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

INSERT INTO `card_type` VALUES (1, '周卡', 'WEEKLY', 99.00, 7, 'DAY', '/images/card2.jpg', 1, 1, NOW());
INSERT INTO `card_type` VALUES (2, '月卡', 'MONTHLY', 199.00, 1, 'MONTH', '/images/card3.jpg', 1, 2, NOW());
INSERT INTO `card_type` VALUES (3, '年卡', 'YEARLY', 1999.00, 1, 'YEAR', '/images/card4.jpg', 1, 3, NOW());
INSERT INTO `card_type` VALUES (4, '次卡', 'COUNT', 19.90, 1, 'DAY', '/images/card1.jpg', 1, 4, NOW());

-- ----------------------------
-- Table structure for coach
-- ----------------------------
DROP TABLE IF EXISTS `coach`;
CREATE TABLE `coach` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像',
  `specialty` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '专长',
  `experience` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '从业经验',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系电话',
  `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '个人简介',
  `status` int DEFAULT 1 COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

INSERT INTO `coach` VALUES (1, '张健', '/images/coach1.jpg', '增肌塑形', '8年', '13800138001', '国家级健身教练，擅长增肌减脂', 1, NOW());
INSERT INTO `coach` VALUES (2, '李婷', '/images/coach2.jpg', '瑜伽普拉提', '6年', '13800138002', '资深瑜伽导师，专注女性健身', 1, NOW());
INSERT INTO `coach` VALUES (3, '王强', '/images/coach3.jpg', '搏击格斗', '10年', '13800138003', '职业拳击手，转型健身教练', 1, NOW());

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程名称',
  `coach_id` int DEFAULT NULL COMMENT '教练ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '课程描述',
  `capacity` int DEFAULT 20 COMMENT '容纳人数',
  `price` decimal(10, 2) DEFAULT 0.00 COMMENT '价格',
  `image_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '封面图',
  `status` int DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

INSERT INTO `course` VALUES (1, '增肌训练营', 1, '专业增肌课程，包含器械训练和营养指导', 15, 299.00, '/images/course1.jpg', 1, NOW());
INSERT INTO `course` VALUES (2, '瑜伽课堂', 2, '舒缓身心，改善体态，适合各年龄段', 20, 199.00, '/images/course2.jpg', 1, NOW());
INSERT INTO `course` VALUES (3, '搏击操', 3, '燃脂搏击，释放压力，提升协调性', 25, 249.00, '/images/course3.jpg', 1, NOW());
INSERT INTO `course` VALUES (4, '普拉提', 2, '核心力量训练，矫正体态', 18, 229.00, '/images/course4.jpg', 1, NOW());

-- ----------------------------
-- Table structure for equipment
-- ----------------------------
DROP TABLE IF EXISTS `equipment`;
CREATE TABLE `equipment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '器材名称',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '器材位置',
  `status` int DEFAULT 1 COMMENT '状态 0报废 1正常 2维修中',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '器材分类',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `image_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图片地址',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

INSERT INTO `equipment` VALUES (1, '跑步机', 'A区-有氧区', 1, NULL, '10台', NOW(), NULL);
INSERT INTO `equipment` VALUES (2, '哑铃组', 'B区-力量区', 1, NULL, '5-30kg各一副', NOW(), NULL);
INSERT INTO `equipment` VALUES (3, '史密斯机', 'C区-综合区', 1, NULL, '深蹲架', NOW(), NULL);
INSERT INTO `equipment` VALUES (4, '动感单车', 'A区-有氧区', 1, NULL, '10台', NOW(), NULL);
INSERT INTO `equipment` VALUES (5, '椭圆机', 'A区-有氧区', 0, NULL, '待维修', NOW(), NULL);

-- ----------------------------
-- Table structure for gym_config
-- ----------------------------
DROP TABLE IF EXISTS `gym_config`;
CREATE TABLE `gym_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '配置值',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key` (`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

INSERT INTO `gym_config` (`config_key`, `config_value`) VALUES
('hero_tag', '2026 新赛季'),
('brand_story', 'GYMCORE 致力于打造高端健身空间。我们汇聚顶尖教练团队，引进国际品牌器械，为会员提供科学、专业、个性化的健身服务。'),
('pay_qr', ''),
('store_address', '[]'),
('promo_video', '/promo.mp4'),
('cta_bg', '');

-- ----------------------------
-- Table structure for member
-- ----------------------------
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '/avatar/default.jpg',
  `gender` int DEFAULT NULL COMMENT '性别 0女 1男',
  `age` int DEFAULT NULL COMMENT '年龄',
  `status` int DEFAULT 1 COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username` (`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
