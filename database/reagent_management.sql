-- 实验室化学试剂与耗材库存管理系统数据库

CREATE DATABASE IF NOT EXISTS reagent_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE reagent_management;

-- 1. 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `role` VARCHAR(20) NOT NULL COMMENT '角色: STUDENT-学生, TEACHER-老师/管理员, ADMIN-系统管理员',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `department` VARCHAR(100) COMMENT '部门/班级',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 试剂分类表
DROP TABLE IF EXISTS `reagent_category`;
CREATE TABLE `reagent_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `code` VARCHAR(50) COMMENT '分类编码',
  `description` VARCHAR(200) COMMENT '描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试剂分类表';

-- 3. 存放位置表
DROP TABLE IF EXISTS `storage_location`;
CREATE TABLE `storage_location` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_name` VARCHAR(100) NOT NULL COMMENT '房间名称',
  `cabinet_no` VARCHAR(50) COMMENT '柜号',
  `shelf_no` VARCHAR(50) COMMENT '货架号',
  `full_location` VARCHAR(200) COMMENT '完整位置',
  `description` VARCHAR(200) COMMENT '描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存放位置表';

-- 4. 试剂信息表
DROP TABLE IF EXISTS `reagent`;
CREATE TABLE `reagent` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '试剂名称',
  `cas_no` VARCHAR(50) COMMENT 'CAS号',
  `category_id` BIGINT COMMENT '分类ID',
  `specification` VARCHAR(100) COMMENT '规格型号',
  `unit` VARCHAR(20) DEFAULT '瓶' COMMENT '单位',
  `manufacturer` VARCHAR(200) COMMENT '生产厂家',
  `danger_level` VARCHAR(20) COMMENT '危险等级',
  `description` VARCHAR(500) COMMENT '描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试剂信息表';

-- 5. 库存表
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reagent_id` BIGINT NOT NULL COMMENT '试剂ID',
  `location_id` BIGINT COMMENT '存放位置ID',
  `batch_no` VARCHAR(50) COMMENT '批次号',
  `quantity` DECIMAL(10,2) DEFAULT 0 COMMENT '当前库存数量',
  `warning_threshold` DECIMAL(10,2) DEFAULT 10 COMMENT '预警阈值',
  `expiry_date` DATE COMMENT '有效期',
  `supplier` VARCHAR(200) COMMENT '供应商',
  `purchase_date` DATE COMMENT '采购日期',
  `purchase_price` DECIMAL(10,2) COMMENT '采购单价',
  `status` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态: NORMAL-正常, LOW-库存不足, EXPIRING-即将过期, EXPIRED-已过期',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_reagent` (`reagent_id`),
  KEY `idx_location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 6. 入库记录表
DROP TABLE IF EXISTS `stock_in_record`;
CREATE TABLE `stock_in_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reagent_id` BIGINT NOT NULL COMMENT '试剂ID',
  `inventory_id` BIGINT COMMENT '库存ID',
  `batch_no` VARCHAR(50) COMMENT '批次号',
  `quantity` DECIMAL(10,2) NOT NULL COMMENT '入库数量',
  `expiry_date` DATE COMMENT '有效期',
  `supplier` VARCHAR(200) COMMENT '供应商',
  `purchase_price` DECIMAL(10,2) COMMENT '采购单价',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) COMMENT '操作人姓名',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  PRIMARY KEY (`id`),
  KEY `idx_reagent` (`reagent_id`),
  KEY `idx_operator` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库记录表';

-- 7. 出库记录表
DROP TABLE IF EXISTS `stock_out_record`;
CREATE TABLE `stock_out_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reagent_id` BIGINT NOT NULL COMMENT '试剂ID',
  `inventory_id` BIGINT COMMENT '库存ID',
  `application_id` BIGINT COMMENT '申请单ID',
  `quantity` DECIMAL(10,2) NOT NULL COMMENT '出库数量',
  `recipient_id` BIGINT NOT NULL COMMENT '领用人ID',
  `recipient_name` VARCHAR(50) COMMENT '领用人姓名',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) COMMENT '操作人姓名',
  `purpose` VARCHAR(500) COMMENT '用途',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '出库时间',
  PRIMARY KEY (`id`),
  KEY `idx_reagent` (`reagent_id`),
  KEY `idx_recipient` (`recipient_id`),
  KEY `idx_application` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库记录表';

-- 8. 领用申请表
DROP TABLE IF EXISTS `application`;
CREATE TABLE `application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `application_no` VARCHAR(50) NOT NULL COMMENT '申请单号',
  `reagent_id` BIGINT NOT NULL COMMENT '试剂ID',
  `reagent_name` VARCHAR(100) COMMENT '试剂名称',
  `quantity` DECIMAL(10,2) NOT NULL COMMENT '申请数量',
  `purpose` VARCHAR(500) NOT NULL COMMENT '用途说明',
  `applicant_id` BIGINT NOT NULL COMMENT '申请人ID',
  `applicant_name` VARCHAR(50) COMMENT '申请人姓名',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝, COMPLETED-已完成',
  `reviewer_id` BIGINT COMMENT '审核人ID',
  `reviewer_name` VARCHAR(50) COMMENT '审核人姓名',
  `review_time` DATETIME COMMENT '审核时间',
  `review_remark` VARCHAR(500) COMMENT '审核备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_application_no` (`application_no`),
  KEY `idx_applicant` (`applicant_id`),
  KEY `idx_reagent` (`reagent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领用申请表';

-- 插入初始数据

-- 插入默认管理员账户
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `email`, `phone`, `department`, `status`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'ADMIN', 'admin@lab.com', '13800000001', '系统管理部', 1),
('teacher', 'e10adc3949ba59abbe56e057f20f883e', '张老师', 'TEACHER', 'teacher@lab.com', '13800000002', '化学实验室', 1),
('student', 'e10adc3949ba59abbe56e057f20f883e', '李同学', 'STUDENT', 'student@lab.com', '13800000003', '化学系2021级', 1);

-- 插入试剂分类
INSERT INTO `reagent_category` (`name`, `code`, `description`) VALUES
('无机酸', 'INORGANIC_ACID', '无机酸类试剂'),
('有机酸', 'ORGANIC_ACID', '有机酸类试剂'),
('碱类', 'ALKALI', '碱类试剂'),
('盐类', 'SALT', '盐类试剂'),
('有机溶剂', 'ORGANIC_SOLVENT', '有机溶剂类试剂'),
('指示剂', 'INDICATOR', '指示剂类试剂'),
('实验耗材', 'CONSUMABLES', '实验耗材');

-- 插入存放位置
INSERT INTO `storage_location` (`room_name`, `cabinet_no`, `shelf_no`, `full_location`, `description`) VALUES
('101实验室', 'A1', '1层', '101实验室-A1柜-1层', '酸类试剂存放区'),
('101实验室', 'A1', '2层', '101实验室-A1柜-2层', '酸类试剂存放区'),
('101实验室', 'B1', '1层', '101实验室-B1柜-1层', '碱类试剂存放区'),
('102实验室', 'C1', '1层', '102实验室-C1柜-1层', '有机溶剂存放区'),
('103实验室', 'D1', '1层', '103实验室-D1柜-1层', '耗材存放区');

-- 插入试剂信息
INSERT INTO `reagent` (`name`, `cas_no`, `category_id`, `specification`, `unit`, `manufacturer`, `danger_level`, `description`) VALUES
('盐酸', '7647-01-0', 1, '500mL/瓶 分析纯', '瓶', '国药集团化学试剂有限公司', '腐蚀品', '强酸，具有强腐蚀性'),
('硫酸', '7664-93-9', 1, '500mL/瓶 分析纯', '瓶', '国药集团化学试剂有限公司', '腐蚀品', '强酸，具有强腐蚀性和脱水性'),
('硝酸', '7697-37-2', 1, '500mL/瓶 分析纯', '瓶', '国药集团化学试剂有限公司', '腐蚀品', '强酸，强氧化性'),
('氢氧化钠', '1310-73-2', 3, '500g/瓶 分析纯', '瓶', '天津市大茂化学试剂厂', '腐蚀品', '强碱，具有强腐蚀性'),
('乙醇', '64-17-5', 5, '500mL/瓶 分析纯', '瓶', '天津市科密欧化学试剂有限公司', '易燃液体', '常用有机溶剂'),
('氯化钠', '7647-14-5', 4, '500g/瓶 分析纯', '瓶', '西陇科学股份有限公司', '普通试剂', '常用盐类试剂'),
('酚酞指示剂', '77-09-8', 6, '25g/瓶', '瓶', '阿拉丁试剂', '普通试剂', '酸碱指示剂'),
('一次性手套', 'N/A', 7, '100只/盒', '盒', '实验耗材公司', '普通耗材', '实验防护用品'),
('滤纸', 'N/A', 7, '100张/盒', '盒', '实验耗材公司', '普通耗材', '过滤用耗材');

-- 插入库存信息
INSERT INTO `inventory` (`reagent_id`, `location_id`, `batch_no`, `quantity`, `warning_threshold`, `expiry_date`, `supplier`, `purchase_date`, `purchase_price`, `status`) VALUES
(1, 1, 'HCL20240101', 50.00, 10.00, '2025-12-31', '国药集团', '2024-01-15', 25.50, 'NORMAL'),
(2, 1, 'H2SO420240102', 35.00, 10.00, '2025-12-31', '国药集团', '2024-01-15', 28.00, 'NORMAL'),
(3, 2, 'HNO320240103', 8.00, 10.00, '2025-11-30', '国药集团', '2024-01-15', 32.00, 'LOW'),
(4, 3, 'NAOH20240104', 45.00, 10.00, '2026-06-30', '天津大茂', '2024-02-01', 18.50, 'NORMAL'),
(5, 4, 'ETOH20240105', 100.00, 20.00, '2025-12-31', '科密欧', '2024-03-01', 15.00, 'NORMAL'),
(6, 1, 'NACL20240106', 80.00, 15.00, '2027-12-31', '西陇科学', '2024-03-15', 12.00, 'NORMAL'),
(7, 2, 'PP20240107', 25.00, 5.00, '2026-09-30', '阿拉丁', '2024-04-01', 45.00, 'NORMAL'),
(8, 5, 'GLOVE20240108', 150.00, 30.00, '2026-12-31', '耗材公司', '2024-05-01', 8.00, 'NORMAL'),
(9, 5, 'PAPER20240109', 200.00, 50.00, '2027-12-31', '耗材公司', '2024-05-15', 5.50, 'NORMAL');

-- 注意：密码为 MD5('123456') = e10adc3949ba59abbe56e057f20f883e







