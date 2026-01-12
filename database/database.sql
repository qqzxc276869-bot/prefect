-- =============================================
-- 试剂管理系统数据库完整脚本
-- 合并版本 - 仅包含核心功能表
-- =============================================

DROP DATABASE IF EXISTS reagent_management;
CREATE DATABASE reagent_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE reagent_management;

-- =============================================
-- 核心表结构
-- =============================================

-- 1. 用户表
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `role` VARCHAR(20) NOT NULL COMMENT '角色: ADMIN-管理员, TEACHER-教师, STUDENT-学生',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `department` VARCHAR(100) COMMENT '所属部门',
  `status` INT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2. 试剂分类表
CREATE TABLE `reagent_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `code` VARCHAR(50),
  `description` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试剂分类表';

-- 3. 试剂基础信息表
CREATE TABLE `reagent` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `cas_no` VARCHAR(50),
  `category_id` BIGINT,
  `specification` VARCHAR(100),
  `unit` VARCHAR(20),
  `manufacturer` VARCHAR(200),
  `supplier_lead_time` INT,
  `danger_level` VARCHAR(50),
  `description` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_cas_no` (`cas_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试剂基础信息表';

-- 4. 存放位置表
CREATE TABLE `storage_location` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `room_name` VARCHAR(100),
  `cabinet_no` VARCHAR(50),
  `shelf_no` VARCHAR(50),
  `full_location` VARCHAR(300),
  `description` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存放位置表';

-- 5. 库存表
CREATE TABLE `inventory` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `reagent_id` BIGINT NOT NULL,
  `location_id` BIGINT,
  `batch_no` VARCHAR(100),
  `quantity` DECIMAL(10,2) DEFAULT 0,
  `warning_threshold` DECIMAL(10,2),
  `expiry_date` DATE,
  `supplier` VARCHAR(200),
  `purchase_price` DECIMAL(10,2),
  `unit_price` DECIMAL(10,2),
  `purchase_date` DATE,
  `production_date` DATE,
  `status` VARCHAR(20) DEFAULT 'NORMAL',
  `remark` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_reagent` (`reagent_id`),
  KEY `idx_location` (`location_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 6. 入库记录表
CREATE TABLE `stock_in_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `reagent_id` BIGINT NOT NULL,
  `inventory_id` BIGINT,
  `location_id` BIGINT,
  `batch_no` VARCHAR(100),
  `quantity` DECIMAL(10,2) NOT NULL,
  `expiry_date` DATE,
  `supplier` VARCHAR(200),
  `purchase_price` DECIMAL(10,2),
  `operator_id` BIGINT,
  `operator_name` VARCHAR(50),
  `remark` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_reagent` (`reagent_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库记录表';

-- 7. 出库记录表
CREATE TABLE `stock_out_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `reagent_id` BIGINT NOT NULL,
  `inventory_id` BIGINT,
  `application_id` BIGINT,
  `quantity` DECIMAL(10,2) NOT NULL,
  `recipient_id` BIGINT,
  `recipient_name` VARCHAR(50),
  `operator_id` BIGINT,
  `operator_name` VARCHAR(50),
  `purpose` VARCHAR(500),
  `remark` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_reagent` (`reagent_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库记录表';

-- 8. 领用申请表
CREATE TABLE `application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `application_no` VARCHAR(50) NOT NULL,
  `reagent_id` BIGINT NOT NULL,
  `reagent_name` VARCHAR(200),
  `quantity` DECIMAL(10,2) NOT NULL,
  `purpose` VARCHAR(500),
  `applicant_id` BIGINT,
  `applicant_name` VARCHAR(50),
  `status` VARCHAR(20) DEFAULT 'PENDING',
  `reviewer_id` BIGINT,
  `reviewer_name` VARCHAR(50),
  `review_time` DATETIME,
  `review_remark` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_application_no` (`application_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领用申请表';

-- 9. 系统公告表
CREATE TABLE `announcement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NOT NULL,
  `audience` VARCHAR(20) DEFAULT 'ALL',
  `priority` VARCHAR(20) DEFAULT 'INFO',
  `creator_id` BIGINT,
  `creator_name` VARCHAR(50),
  `publish_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- 10. 问题反馈表
CREATE TABLE `feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `user_name` VARCHAR(50),
  `feedback_type` VARCHAR(20) DEFAULT 'SYSTEM',
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NOT NULL,
  `status` VARCHAR(20) DEFAULT 'PENDING',
  `handler_id` BIGINT,
  `handler_name` VARCHAR(50),
  `handle_remark` TEXT,
  `handle_time` DATETIME,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题反馈表';

-- 11. 预测记录表
CREATE TABLE `forecasting_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `reagent_id` BIGINT NOT NULL,
  `reagent_name` VARCHAR(200),
  `current_stock` DECIMAL(10,2),
  `predicted_depletion_date` DATE,
  `days_until_depletion` INT,
  `average_daily_consumption` DECIMAL(10,4),
  `recommended_order_quantity` DECIMAL(10,2),
  `confidence_level` DECIMAL(5,2),
  `status` VARCHAR(20) DEFAULT 'ACTIVE',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_reagent` (`reagent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预测记录表';

-- =============================================
-- 样本数据插入
-- =============================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. 用户数据（密码: 123456, MD5: e10adc3949ba59abbe56e057f20f883e）
INSERT INTO sys_user (username, password, real_name, role, email, phone, department, status) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'ADMIN', 'admin@lab.com', '13800138000', '实验室管理部', 1),
('teacher1', 'e10adc3949ba59abbe56e057f20f883e', '张教授', 'TEACHER', 'zhang@lab.com', '13800138001', '化学系', 1),
('teacher2', 'e10adc3949ba59abbe56e057f20f883e', '李老师', 'TEACHER', 'li@lab.com', '13800138002', '生物系', 1),
('teacher3', 'e10adc3949ba59abbe56e057f20f883e', '王老师', 'TEACHER', 'wang@lab.com', '13800138003', '材料系', 1),
('student1', 'e10adc3949ba59abbe56e057f20f883e', '王小明', 'STUDENT', 'wangxm@lab.com', '13900139001', '化学系', 1),
('student2', 'e10adc3949ba59abbe56e057f20f883e', '刘小红', 'STUDENT', 'liuxh@lab.com', '13900139002', '化学系', 1),
('student3', 'e10adc3949ba59abbe56e057f20f883e', '陈小华', 'STUDENT', 'chenxh@lab.com', '13900139003', '生物系', 1),
('student4', 'e10adc3949ba59abbe56e057f20f883e', '赵敏', 'STUDENT', 'zhaom@lab.com', '13900139004', '材料系', 1),
('student5', 'e10adc3949ba59abbe56e057f20f883e', '孙涛', 'STUDENT', 'sunt@lab.com', '13900139005', '化学系', 1);

-- 2. 试剂分类数据
INSERT INTO reagent_category (name, code, description) VALUES
('有机溶剂', 'ORG_SOLVENT', '各类有机溶剂，如乙醇、甲醇、丙酮等'),
('无机酸碱', 'INORG_ACID_BASE', '无机酸和碱类试剂'),
('标准试剂', 'STANDARD', '标准物质和标准溶液'),
('生物试剂', 'BIO_REAGENT', '生物实验用试剂'),
('指示剂', 'INDICATOR', '各类指示剂'),
('金属盐类', 'METAL_SALT', '各类金属盐'),
('有机化合物', 'ORGANIC_COMPOUND', '各类有机化合物'),
('气体', 'GAS', '各类实验用气体');

-- 3. 试剂基础信息数据（30种常用试剂）
INSERT INTO reagent (name, cas_no, category_id, specification, unit, manufacturer, supplier_lead_time, danger_level, description) VALUES
('乙醇', '64-17-5', 1, '分析纯AR 500ml', '瓶', '国药集团', 3, '易燃', '无色透明液体，易挥发'),
('甲醇', '67-56-1', 1, '分析纯AR 500ml', '瓶', '国药集团', 3, '易燃有毒', '无色透明液体，有毒'),
('丙酮', '67-64-1', 1, '分析纯AR 500ml', '瓶', '西陇科学', 5, '易燃', '无色透明液体'),
('乙酸乙酯', '141-78-6', 1, '分析纯AR 500ml', '瓶', '国药集团', 4, '易燃', '常用有机溶剂'),
('二氯甲烷', '75-09-2', 1, '分析纯AR 500ml', '瓶', '西陇科学', 5, '有毒', '挥发性强'),
('正己烷', '110-54-3', 1, '分析纯AR 500ml', '瓶', '国药集团', 4, '易燃', '非极性溶剂'),
('盐酸', '7647-01-0', 2, '优级纯GR 36-38% 500ml', '瓶', '国药集团', 2, '腐蚀性', '强酸'),
('硫酸', '7664-93-9', 2, '优级纯GR 95-98% 500ml', '瓶', '国药集团', 2, '强腐蚀', '浓硫酸'),
('硝酸', '7697-37-2', 2, '优级纯GR 65-68% 500ml', '瓶', '国药集团', 3, '强腐蚀性', '强氧化性酸'),
('氢氧化钠', '1310-73-2', 2, '分析纯AR 500g', '瓶', '西陇科学', 3, '腐蚀性', '强碱'),
('氢氧化钾', '1310-58-3', 2, '分析纯AR 500g', '瓶', '国药集团', 4, '腐蚀性', '强碱'),
('氨水', '1336-21-6', 2, '分析纯AR 25-28% 500ml', '瓶', '西陇科学', 3, '刺激性', '碱性溶液'),
('氯化钠', '7647-14-5', 6, '分析纯AR 500g', '瓶', '国药集团', 3, '无危险', '白色晶体'),
('氯化钾', '7447-40-7', 6, '分析纯AR 500g', '瓶', '国药集团', 3, '无危险', '白色晶体'),
('硫酸铜', '7758-98-7', 6, '分析纯AR 500g', '瓶', '西陇科学', 4, '有害', '蓝色晶体'),
('硝酸银', '7761-88-8', 6, '分析纯AR 25g', '瓶', '上海试剂三厂', 7, '氧化性', '见光分解'),
('高锰酸钾', '7722-64-7', 6, '分析纯AR 100g', '瓶', '西陇科学', 3, '强氧化剂', '紫黑色晶体'),
('碘', '7553-56-2', 6, '分析纯AR 50g', '瓶', '上海试剂三厂', 6, '腐蚀性', '易升华'),
('葡萄糖', '50-99-7', 7, '分析纯AR 500g', '瓶', '国药集团', 3, '无危险', '白色晶体粉末'),
('蔗糖', '57-50-1', 7, '分析纯AR 500g', '瓶', '国药集团', 3, '无危险', '白色晶体'),
('乙酸', '64-19-7', 1, '分析纯AR 500ml', '瓶', '西陇科学', 4, '腐蚀性', '冰醋酸'),
('苯', '71-43-2', 1, '分析纯AR 500ml', '瓶', '国药集团', 5, '易燃有毒', '芳香烃'),
('甲苯', '108-88-3', 1, '分析纯AR 500ml', '瓶', '西陇科学', 5, '易燃', '芳香烃'),
('酚酞指示剂', '77-09-8', 5, '1% 乙醇溶液 100ml', '瓶', '天津科密欧', 5, '低危', '指示剂'),
('甲基橙指示剂', '547-58-0', 5, '0.1% 水溶液 100ml', '瓶', '天津科密欧', 5, '低危', '指示剂'),
('氯化钙', '10043-52-4', 6, '分析纯AR 500g', '瓶', '国药集团', 3, '无危险', '干燥剂'),
('无水硫酸钠', '7757-82-6', 6, '分析纯AR 500g', '瓶', '国药集团', 3, '无危险', '干燥剂'),
('碳酸钠', '497-19-8', 6, '分析纯AR 500g', '瓶', '西陇科学', 3, '刺激性', '碱性盐'),
('碳酸氢钠', '144-55-8', 6, '分析纯AR 500g', '瓶', '国药集团', 3, '无危险', '小苏打'),
('硼酸', '10043-35-3', 2, '分析纯AR 500g', '瓶', '国药集团', 4, '低毒', '弱酸');

-- 4. 存放位置数据
INSERT INTO storage_location (room_name, cabinet_no, shelf_no, full_location, description) VALUES
('化学实验室A', 'A01', 'L1', '化学实验室A-A01-L1', '普通化学品存放区第1层'),
('化学实验室A', 'A01', 'L2', '化学实验室A-A01-L2', '普通化学品存放区第2层'),
('化学实验室A', 'A01', 'L3', '化学实验室A-A01-L3', '普通化学品存放区第3层'),
('化学实验室A', 'A02', 'L1', '化学实验室A-A02-L1', '酸碱类试剂存放区第1层'),
('化学实验室A', 'A02', 'L2', '化学实验室A-A02-L2', '酸碱类试剂存放区第2层'),
('化学实验室B', 'B01', 'L1', '化学实验室B-B01-L1', '有机溶剂存放区第1层'),
('化学实验室B', 'B01', 'L2', '化学实验室B-B01-L2', '有机溶剂存放区第2层'),
('化学实验室B', 'B02', 'L1', '化学实验室B-B02-L1', '特殊试剂存放区'),
('生物实验室', 'C01', 'L1', '生物实验室-C01-L1', '生物试剂存放区'),
('危险品库', 'D01', 'L1', '危险品库-D01-L1', '易燃易爆品专用柜'),
('材料实验室', 'E01', 'L1', '材料实验室-E01-L1', '材料试剂存放区'),
('分析室', 'F01', 'L1', '分析室-F01-L1', '标准品存放区');

-- 5. 库存数据（包含多种状态：正常、库存不足、即将过期、已过期）
INSERT INTO inventory (reagent_id, location_id, batch_no, quantity, warning_threshold, expiry_date, supplier, purchase_price, unit_price, purchase_date, production_date, status, remark) VALUES
-- 正常状态试剂
(1, 6, 'ET-2024-001', 25.00, 5.00, '2025-12-31', '国药集团', 750.00, 30.00, '2024-01-15', '2023-12-10', 'NORMAL', '常用试剂'),
(2, 6, 'ME-2024-002', 12.00, 3.00, '2025-11-30', '国药集团', 600.00, 50.00, '2024-01-20', '2023-12-15', 'NORMAL', ''),
(3, 6, 'AC-2024-003', 18.00, 5.00, '2025-10-31', '西陇科学', 540.00, 30.00, '2024-02-01', '2024-01-10', 'NORMAL', ''),
(4, 6, 'EE-2024-004', 10.00, 3.00, '2025-09-30', '国药集团', 400.00, 40.00, '2024-02-10', '2024-01-15', 'NORMAL', ''),
(5, 6, 'DCM-2024-005', 8.00, 2.00, '2025-08-31', '西陇科学', 480.00, 60.00, '2024-02-15', '2024-01-20', 'NORMAL', ''),
(6, 6, 'HEX-2024-006', 15.00, 4.00, '2025-12-31', '国药集团', 600.00, 40.00, '2024-02-20', '2024-01-25', 'NORMAL', ''),
(7, 4, 'HCL-2024-007', 30.00, 8.00, '2026-06-30', '国药集团', 900.00, 30.00, '2024-03-01', '2024-02-01', 'NORMAL', '浓度36-38%'),
(8, 4, 'H2SO4-2024-008', 25.00, 6.00, '2026-12-31', '国药集团', 1000.00, 40.00, '2024-03-05', '2024-02-05', 'NORMAL', '浓度95-98%'),
(9, 4, 'HNO3-2024-009', 20.00, 5.00, '2026-09-30', '国药集团', 800.00, 40.00, '2024-03-10', '2024-02-10', 'NORMAL', ''),
(10, 5, 'NAOH-2024-010', 35.00, 10.00, '2027-03-31', '西陇科学', 525.00, 15.00, '2024-03-15', '2024-02-15', 'NORMAL', 'AI建议申请量500g'),
(11, 5, 'KOH-2024-011', 20.00, 5.00, '2027-02-28', '国药集团', 400.00, 20.00, '2024-03-20', '2024-02-20', 'NORMAL', ''),
(12, 5, 'NH3-2024-012', 15.00, 4.00, '2025-12-31', '西陇科学', 450.00, 30.00, '2024-03-25', '2024-02-25', 'NORMAL', ''),
(13, 1, 'NACL-2024-013', 50.00, 15.00, '2028-12-31', '国药集团', 500.00, 10.00, '2024-04-01', '2024-03-01', 'NORMAL', ''),
(14, 1, 'KCL-2024-014', 40.00, 10.00, '2028-12-31', '国药集团', 600.00, 15.00, '2024-04-05', '2024-03-05', 'NORMAL', ''),
(15, 1, 'CUSO4-2024-015', 12.00, 3.00, '2027-12-31', '西陇科学', 360.00, 30.00, '2024-04-10', '2024-03-10', 'NORMAL', ''),
-- 库存不足状态
(16, 2, 'AGN-2024-016', 2.00, 5.00, '2026-12-31', '上海试剂三厂', 400.00, 200.00, '2024-04-15', '2024-03-15', 'LOW', '贵重试剂，库存不足'),
(17, 2, 'KMNO4-2024-017', 10.00, 3.00, '2027-12-31', '西陇科学', 300.00, 30.00, '2024-04-20', '2024-03-20', 'NORMAL', ''),
(18, 2, 'IOD-2024-018', 1.50, 5.00, '2026-12-31', '上海试剂三厂', 300.00, 200.00, '2024-04-25', '2024-03-25', 'LOW', '易升华，库存不足'),
(19, 1, 'GLU-2024-019', 40.00, 10.00, '2026-12-31', '国药集团', 600.00, 15.00, '2024-05-01', '2024-04-01', 'NORMAL', ''),
(20, 1, 'SUC-2024-020', 30.00, 8.00, '2026-12-31', '国药集团', 450.00, 15.00, '2024-05-05', '2024-04-05', 'NORMAL', ''),
(21, 6, 'AA-2024-021', 8.00, 3.00, '2025-09-30', '西陇科学', 400.00, 50.00, '2024-05-10', '2024-04-10', 'NORMAL', '冰醋酸'),
(22, 10, 'BEN-2024-022', 5.00, 2.00, '2025-08-31', '国药集团', 400.00, 80.00, '2024-05-15', '2024-04-15', 'NORMAL', '有毒'),
(23, 10, 'TOL-2024-023', 8.00, 2.00, '2025-10-31', '西陇科学', 480.00, 60.00, '2024-05-20', '2024-04-20', 'NORMAL', ''),
(24, 1, 'PP-2024-024', 10.00, 2.00, '2025-12-31', '天津科密欧', 150.00, 15.00, '2024-05-25', '2024-04-25', 'NORMAL', ''),
(25, 1, 'MO-2024-025', 10.00, 2.00, '2025-12-31', '天津科密欧', 150.00, 15.00, '2024-05-30', '2024-04-30', 'NORMAL', ''),
(26, 1, 'CACL2-2024-026', 25.00, 5.00, '2028-12-31', '国药集团', 375.00, 15.00, '2024-06-01', '2024-05-01', 'NORMAL', ''),
(27, 1, 'NA2SO4-2024-027', 20.00, 5.00, '2028-12-31', '国药集团', 300.00, 15.00, '2024-06-05', '2024-05-05', 'NORMAL', ''),
(28, 1, 'NA2CO3-2024-028', 30.00, 8.00, '2027-12-31', '西陇科学', 450.00, 15.00, '2024-06-10', '2024-05-10', 'NORMAL', ''),
(29, 1, 'NAHCO3-2024-029', 35.00, 10.00, '2027-12-31', '国药集团', 350.00, 10.00, '2024-06-15', '2024-05-15', 'NORMAL', ''),
(30, 1, 'H3BO3-2024-030', 15.00, 4.00, '2027-06-30', '国药集团', 450.00, 30.00, '2024-06-20', '2024-05-20', 'NORMAL', ''),
-- 即将过期状态 (30天内过期)
(1, 7, 'ET-2024-031', 12.00, 5.00, '2026-02-10', '国药集团', 360.00, 30.00, '2024-06-25', '2024-06-01', 'EXPIRING', '即将过期，请尽快使用'),
(2, 7, 'ME-2024-032', 8.00, 3.00, '2026-02-15', '国药集团', 400.00, 50.00, '2024-06-28', '2024-06-05', 'EXPIRING', '即将过期'),
(3, 7, 'AC-2024-033', 10.00, 5.00, '2026-02-20', '西陇科学', 300.00, 30.00, '2024-07-01', '2024-06-10', 'EXPIRING', '即将过期'),
(6, 7, 'HEX-2024-034', 6.00, 4.00, '2026-02-25', '国药集团', 240.00, 40.00, '2024-07-05', '2024-06-15', 'EXPIRING', '即将过期'),
(12, 5, 'NH3-2024-035', 5.00, 4.00, '2026-02-28', '西陇科学', 150.00, 30.00, '2024-07-08', '2024-06-18', 'EXPIRING', '即将过期，请及时使用'),
-- 已过期状态
(4, 8, 'EE-2023-001', 5.00, 3.00, '2025-12-31', '国药集团', 200.00, 40.00, '2023-06-01', '2023-05-01', 'EXPIRED', '已过期，待处理'),
(5, 8, 'DCM-2023-002', 3.00, 2.00, '2025-11-30', '西陇科学', 180.00, 60.00, '2023-07-01', '2023-06-01', 'EXPIRED', '已过期，待处理'),
(21, 8, 'AA-2023-003', 4.00, 3.00, '2025-10-31', '西陇科学', 200.00, 50.00, '2023-08-01', '2023-07-01', 'EXPIRED', '已过期，待销毁'),
(22, 8, 'BEN-2023-004', 2.00, 2.00, '2025-09-30', '国药集团', 160.00, 80.00, '2023-09-01', '2023-08-01', 'EXPIRED', '已过期，有毒待处理'),
-- 额外的正常库存（用于二维码管理测试）
(1, 1, 'ET-2024-QR01', 20.00, 5.00, '2026-06-30', '国药集团', 600.00, 30.00, '2024-07-10', '2024-06-20', 'NORMAL', '带二维码标识'),
(7, 4, 'HCL-2024-QR02', 25.00, 8.00, '2026-12-31', '国药集团', 750.00, 30.00, '2024-07-12', '2024-06-22', 'NORMAL', '带二维码标识'),
(8, 4, 'H2SO4-2024-QR03', 20.00, 6.00, '2027-03-31', '国药集团', 800.00, 40.00, '2024-07-14', '2024-06-24', 'NORMAL', '带二维码标识'),
(10, 5, 'NAOH-2024-QR04', 30.00, 10.00, '2027-06-30', '西陇科学', 450.00, 15.00, '2024-07-16', '2024-06-26', 'NORMAL', '带二维码标识，AI建议500g'),
(13, 1, 'NACL-2024-QR05', 45.00, 15.00, '2028-06-30', '国药集团', 450.00, 10.00, '2024-07-18', '2024-06-28', 'NORMAL', '带二维码标识'),
(16, 2, 'AGN-2024-QR06', 3.00, 5.00, '2027-12-31', '上海试剂三厂', 600.00, 200.00, '2024-07-20', '2024-06-30', 'NORMAL', '带二维码标识，贵重'),
(17, 2, 'KMNO4-2024-QR07', 12.00, 3.00, '2027-09-30', '西陇科学', 360.00, 30.00, '2024-07-22', '2024-07-02', 'NORMAL', '带二维码标识'),
(19, 1, 'GLU-2024-QR08', 35.00, 10.00, '2027-06-30', '国药集团', 525.00, 15.00, '2024-07-24', '2024-07-04', 'NORMAL', '带二维码标识'),
-- 更多库存不足的试剂
(2, 7, 'ME-2024-LOW01', 2.50, 10.00, '2026-03-31', '国药集团', 125.00, 50.00, '2024-07-26', '2024-07-06', 'LOW', '库存严重不足'),
(4, 7, 'EE-2024-LOW02', 1.80, 8.00, '2026-04-30', '国药集团', 72.00, 40.00, '2024-07-28', '2024-07-08', 'LOW', '库存严重不足'),
(9, 4, 'HNO3-2024-LOW03', 3.00, 15.00, '2026-12-31', '国药集团', 120.00, 40.00, '2024-07-30', '2024-07-10', 'LOW', '库存不足'),
(11, 5, 'KOH-2024-LOW04', 2.00, 12.00, '2026-09-30', '国药集团', 40.00, 20.00, '2024-08-01', '2024-07-12', 'LOW', '库存不足'),
(15, 1, 'CUSO4-2024-LOW05', 1.00, 8.00, '2027-03-31', '西陇科学', 30.00, 30.00, '2024-08-03', '2024-07-14', 'LOW', '库存严重不足');

-- 6. 领用申请数据
INSERT INTO application (application_no, reagent_id, reagent_name, quantity, purpose, applicant_id, applicant_name, status, reviewer_id, reviewer_name, review_time, review_remark, create_time) VALUES
('APP-2024060001', 1, '乙醇', 2.00, '有机合成实验溶剂', 5, '王小明', 'COMPLETED', 2, '张教授', '2024-06-01 10:30:00', '同意', '2024-06-01 09:00:00'),
('APP-2024060002', 3, '丙酮', 1.00, '样品萃取实验', 6, '刘小红', 'COMPLETED', 2, '张教授', '2024-06-01 14:20:00', '同意', '2024-06-01 11:00:00'),
('APP-2024060003', 7, '盐酸', 0.50, 'pH调节实验', 7, '陈小华', 'COMPLETED', 3, '李老师', '2024-06-02 09:15:00', '同意', '2024-06-02 08:30:00'),
('APP-2024060004', 13, '氯化钠', 1.00, '配制生理盐水', 7, '陈小华', 'APPROVED', 3, '李老师', '2024-06-03 10:00:00', '同意', '2024-06-03 09:00:00'),
('APP-2024060005', 1, '乙醇', 1.50, '有机反应', 5, '王小明', 'PENDING', NULL, NULL, NULL, NULL, '2024-06-04 14:00:00'),
('APP-2024060006', 19, '葡萄糖', 0.50, '生物实验用', 8, '赵敏', 'PENDING', NULL, NULL, NULL, NULL, '2024-06-05 10:00:00'),
('APP-2024060007', 17, '高锰酸钾', 0.10, '氧化还原滴定', 9, '孙涛', 'PENDING', NULL, NULL, NULL, NULL, '2024-06-05 15:00:00');

-- 7. 系统公告数据
INSERT INTO announcement (title, content, audience, priority, creator_id, creator_name, publish_time) VALUES
('欢迎使用试剂管理系统', '欢迎各位老师和同学使用实验室试剂管理系统！

系统功能包括：
- 试剂库存查询和管理
- 试剂申领和审批
- 入库出库记录
- AI智能助手

如有问题，请联系系统管理员。', 'ALL', 'INFO', 1, '系统管理员', '2024-01-01 09:00:00'),
('实验室安全注意事项', '各位实验室成员请注意：

1. 进入实验室必须穿戴实验服、护目镜
2. 使用危险化学品前必须了解MSDS
3. 实验过程中严禁饮食、吸烟
4. 实验结束后清理台面，关闭水电气源
5. 发现安全隐患及时报告

安全第一，预防为主！', 'ALL', 'WARN', 1, '系统管理员', '2024-01-15 10:00:00'),
('试剂申领流程说明', '各位同学请注意试剂申领流程：

1. 登录系统后进入"库存查询"
2. 选择需要申领的试剂
3. 填写详细的用途说明
4. 提交申请后等待老师审批
5. 审批通过后到指定地点领取

请提前申请，避免影响实验进度。', 'STUDENT', 'INFO', 1, '系统管理员', '2024-02-01 14:00:00'),
('库存预警提醒', '【重要通知】

以下试剂库存已低于预警阈值：

1. 硝酸银 - 当前库存：2.0瓶
2. 碘 - 当前库存：1.5瓶

请及时申请采购。', 'ALL', 'WARN', 1, '系统管理员', '2024-06-01 11:00:00');

-- 8. 问题反馈数据
INSERT INTO feedback (user_id, user_name, feedback_type, title, content, status, handler_id, handler_name, handle_remark, handle_time, create_time) VALUES
(5, '王小明', 'SYSTEM', '系统登录速度慢', '最近登录系统时经常需要等待很久，希望能优化一下登录速度。', 'RESOLVED', 1, '系统管理员', '已优化数据库查询，登录速度已提升', '2024-05-15 14:00:00', '2024-05-10 09:00:00'),
(6, '刘小红', 'REAGENT', '乙醇库存不足', '最近乙醇使用量较大，建议增加库存。', 'PROCESSING', 2, '张教授', '已提交采购申请', '2024-05-20 10:00:00', '2024-05-18 11:00:00'),
(7, '陈小华', 'SUGGESTION', '建议增加试剂搜索功能', '希望能够按照CAS号搜索试剂，这样查找更方便。', 'RESOLVED', 1, '系统管理员', '新版本已支持CAS号搜索', '2024-06-01 09:00:00', '2024-05-25 15:00:00'),
(8, '赵敏', 'REAGENT', '硝酸银快过期了', '编号AGN-2024-016的硝酸银还有6个月就要过期了，请注意。', 'PENDING', NULL, NULL, NULL, NULL, '2024-06-03 14:00:00');

-- 9. 入库记录数据
INSERT INTO stock_in_record (reagent_id, inventory_id, location_id, batch_no, quantity, expiry_date, supplier, purchase_price, operator_id, operator_name, remark, create_time) VALUES
(1, 1, 6, 'ET-2024-001', 25.00, '2025-12-31', '国药集团', 750.00, 1, '系统管理员', '采购入库', '2024-01-15 10:00:00'),
(2, 2, 6, 'ME-2024-002', 12.00, '2025-11-30', '国药集团', 600.00, 1, '系统管理员', '采购入库', '2024-01-20 11:00:00'),
(7, 7, 4, 'HCL-2024-007', 30.00, '2026-06-30', '国药集团', 900.00, 1, '系统管理员', '采购入库', '2024-03-01 14:00:00'),
(10, 10, 5, 'NAOH-2024-010', 35.00, '2027-03-31', '西陇科学', 525.00, 1, '系统管理员', '采购入库', '2024-03-15 10:00:00'),
(13, 13, 1, 'NACL-2024-013', 50.00, '2028-12-31', '国药集团', 500.00, 1, '系统管理员', '采购入库', '2024-04-01 09:00:00');

-- 10. 出库记录数据（最近3个月）
INSERT INTO stock_out_record (reagent_id, inventory_id, application_id, quantity, recipient_id, recipient_name, operator_id, operator_name, purpose, remark, create_time) VALUES
(1, 1, 1, 2.00, 5, '王小明', 1, '系统管理员', '有机合成实验溶剂', '已领用', '2024-06-01 11:00:00'),
(3, 3, 2, 1.00, 6, '刘小红', 1, '系统管理员', '样品萃取实验', '已领用', '2024-06-01 15:00:00'),
(7, 7, 3, 0.50, 7, '陈小华', 1, '系统管理员', 'pH调节实验', '已领用', '2024-06-02 10:00:00'),
(1, 1, NULL, 1.50, 5, '王小明', 1, '系统管理员', '实验室清洗', '临时领用', '2024-05-15 09:00:00'),
(3, 3, NULL, 1.00, 6, '刘小红', 1, '系统管理员', '萃取实验', '临时领用', '2024-05-20 14:00:00'),
(13, 13, NULL, 2.00, 7, '陈小华', 1, '系统管理员', '配制缓冲液', '临时领用', '2024-05-25 10:00:00'),
(19, 19, NULL, 0.50, 8, '赵敏', 1, '系统管理员', '生物实验', '临时领用', '2024-05-28 11:00:00'),
(1, 1, NULL, 1.00, 9, '孙涛', 1, '系统管理员', '溶剂提取', '临时领用', '2024-06-03 15:00:00');

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 数据库初始化完成
-- =============================================
