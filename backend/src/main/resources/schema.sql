-- =============================================
-- 试剂管理系统数据库初始化脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS reagent_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE reagent_management;

-- =============================================
-- 1. 用户管理表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `role` VARCHAR(20) NOT NULL COMMENT '角色: ADMIN-管理员, TEACHER-教师, STUDENT-学生',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `department` VARCHAR(100) COMMENT '所属部门',
  `status` INT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- =============================================
-- 2. 试剂分类表
-- =============================================
CREATE TABLE IF NOT EXISTS `reagent_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `code` VARCHAR(50) COMMENT '分类编码',
  `description` TEXT COMMENT '分类描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试剂分类表';

-- =============================================
-- 3. 试剂基础信息表
-- =============================================
CREATE TABLE IF NOT EXISTS `reagent` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '试剂ID',
  `name` VARCHAR(200) NOT NULL COMMENT '试剂名称',
  `cas_no` VARCHAR(50) COMMENT 'CAS号',
  `category_id` BIGINT COMMENT '分类ID',
  `specification` VARCHAR(100) COMMENT '规格',
  `unit` VARCHAR(20) COMMENT '单位',
  `manufacturer` VARCHAR(200) COMMENT '生产厂家',
  `supplier_lead_time` INT COMMENT '供应商到货周期(天)',
  `danger_level` VARCHAR(50) COMMENT '危险等级',
  `description` TEXT COMMENT '描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_cas_no` (`cas_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试剂基础信息表';

-- =============================================
-- 4. 存放位置表
-- =============================================
CREATE TABLE IF NOT EXISTS `storage_location` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '位置ID',
  `room_name` VARCHAR(100) COMMENT '房间名称',
  `cabinet_no` VARCHAR(50) COMMENT '柜子编号',
  `shelf_no` VARCHAR(50) COMMENT '层架编号',
  `full_location` VARCHAR(300) COMMENT '完整位置描述',
  `description` TEXT COMMENT '描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存放位置表';

-- =============================================
-- 5. 库存表
-- =============================================
CREATE TABLE IF NOT EXISTS `inventory` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `reagent_id` BIGINT NOT NULL COMMENT '试剂ID',
  `location_id` BIGINT COMMENT '存放位置ID',
  `batch_no` VARCHAR(100) COMMENT '批次号',
  `quantity` DECIMAL(10,2) DEFAULT 0 COMMENT '数量',
  `warning_threshold` DECIMAL(10,2) COMMENT '预警阈值',
  `expiry_date` DATE COMMENT '有效期',
  `supplier` VARCHAR(200) COMMENT '供应商',
  `purchase_price` DECIMAL(10,2) COMMENT '采购价格',
  `unit_price` DECIMAL(10,2) COMMENT '单价',
  `purchase_date` DATE COMMENT '采购日期',
  `production_date` DATE COMMENT '生产日期',
  `status` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态: NORMAL-正常, LOW-库存低, EXPIRING-即将过期, EXPIRED-已过期',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_reagent` (`reagent_id`),
  KEY `idx_location` (`location_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- =============================================
-- 6. 入库记录表
-- =============================================
CREATE TABLE IF NOT EXISTS `stock_in_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `reagent_id` BIGINT NOT NULL COMMENT '试剂ID',
  `inventory_id` BIGINT COMMENT '库存ID',
  `location_id` BIGINT COMMENT '存放位置ID',
  `batch_no` VARCHAR(100) COMMENT '批次号',
  `quantity` DECIMAL(10,2) NOT NULL COMMENT '入库数量',
  `expiry_date` DATE COMMENT '有效期',
  `supplier` VARCHAR(200) COMMENT '供应商',
  `purchase_price` DECIMAL(10,2) COMMENT '采购价格',
  `operator_id` BIGINT COMMENT '操作员ID',
  `operator_name` VARCHAR(50) COMMENT '操作员姓名',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_reagent` (`reagent_id`),
  KEY `idx_inventory` (`inventory_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库记录表';

-- =============================================
-- 7. 出库记录表
-- =============================================
CREATE TABLE IF NOT EXISTS `stock_out_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `reagent_id` BIGINT NOT NULL COMMENT '试剂ID',
  `inventory_id` BIGINT COMMENT '库存ID',
  `application_id` BIGINT COMMENT '申请单ID',
  `quantity` DECIMAL(10,2) NOT NULL COMMENT '出库数量',
  `recipient_id` BIGINT COMMENT '领用人ID',
  `recipient_name` VARCHAR(50) COMMENT '领用人姓名',
  `operator_id` BIGINT COMMENT '操作员ID',
  `operator_name` VARCHAR(50) COMMENT '操作员姓名',
  `purpose` VARCHAR(500) COMMENT '用途',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_reagent` (`reagent_id`),
  KEY `idx_inventory` (`inventory_id`),
  KEY `idx_application` (`application_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库记录表';

-- =============================================
-- 8. 领用申请表
-- =============================================
CREATE TABLE IF NOT EXISTS `application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `application_no` VARCHAR(50) NOT NULL COMMENT '申请单号',
  `reagent_id` BIGINT NOT NULL COMMENT '试剂ID',
  `reagent_name` VARCHAR(200) COMMENT '试剂名称',
  `quantity` DECIMAL(10,2) NOT NULL COMMENT '申请数量',
  `purpose` VARCHAR(500) COMMENT '用途',
  `applicant_id` BIGINT COMMENT '申请人ID',
  `applicant_name` VARCHAR(50) COMMENT '申请人姓名',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝, COMPLETED-已完成',
  `reviewer_id` BIGINT COMMENT '审核人ID',
  `reviewer_name` VARCHAR(50) COMMENT '审核人姓名',
  `review_time` DATETIME COMMENT '审核时间',
  `review_remark` TEXT COMMENT '审核备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_application_no` (`application_no`),
  KEY `idx_reagent` (`reagent_id`),
  KEY `idx_applicant` (`applicant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领用申请表';

-- =============================================
-- 9. 系统公告表
-- =============================================
CREATE TABLE IF NOT EXISTS `announcement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
  `content` TEXT NOT NULL COMMENT '公告内容',
  `audience` VARCHAR(20) DEFAULT 'ALL' COMMENT '受众：ALL/TEACHER/STUDENT',
  `priority` VARCHAR(20) DEFAULT 'INFO' COMMENT '级别：INFO/WARN/URGENT',
  `creator_id` BIGINT COMMENT '发布人ID',
  `creator_name` VARCHAR(50) COMMENT '发布人姓名',
  `publish_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_audience` (`audience`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- =============================================
-- 17. 试剂生命周期表
-- =============================================
CREATE TABLE IF NOT EXISTS `reagent_lifecycle` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `reagent_id` BIGINT NOT NULL COMMENT '试剂ID',
  `inventory_id` BIGINT COMMENT '库存ID',
  `batch_no` VARCHAR(100) COMMENT '批次号',
  `lifecycle_stage` VARCHAR(50) COMMENT '生命周期阶段',
  `event_type` VARCHAR(50) COMMENT '事件类型',
  `event_description` TEXT COMMENT '事件描述',
  `operator_id` BIGINT COMMENT '操作人ID',
  `operator_name` VARCHAR(50) COMMENT '操作人姓名',
  `event_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
  PRIMARY KEY (`id`),
  KEY `idx_reagent` (`reagent_id`),
  KEY `idx_inventory` (`inventory_id`),
  KEY `idx_event_time` (`event_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试剂生命周期表';

-- =============================================
-- 18. 存储位置属性表
-- =============================================
CREATE TABLE IF NOT EXISTS `storage_location_attributes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `location_id` BIGINT NOT NULL COMMENT '存储位置ID',
  `is_ventilated` INT DEFAULT 0 COMMENT '是否通风: 0-否, 1-是',
  `is_explosion_proof` INT DEFAULT 0 COMMENT '是否防爆: 0-否, 1-是',
  `allowed_hazard_types` TEXT COMMENT '允许存放的危险品类型(JSON)',
  `forbidden_hazard_types` TEXT COMMENT '禁止存放的危险品类型(JSON)',
  `temperature_control` VARCHAR(50) COMMENT '温度控制',
  `humidity_control` VARCHAR(50) COMMENT '湿度控制',
  `max_capacity` DECIMAL(10,2) COMMENT '最大容量',
  `special_requirements` TEXT COMMENT '特殊要求',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储位置属性表';

-- =============================================
-- 20. 预测记录表
-- =============================================
CREATE TABLE IF NOT EXISTS `forecasting_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `reagent_id` BIGINT NOT NULL COMMENT '试剂ID',
  `forecast_date` DATE COMMENT '预测日期',
  `forecast_quantity` DECIMAL(10,2) COMMENT '预测数量',
  `confidence_level` DECIMAL(5,2) COMMENT '置信度',
  `forecast_period` INT COMMENT '预测周期(天)',
  `algorithm` VARCHAR(50) COMMENT '算法',
  `actual_quantity` DECIMAL(10,2) COMMENT '实际消耗数量',
  `accuracy` DECIMAL(5,2) COMMENT '准确率',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_reagent` (`reagent_id`),
  KEY `idx_forecast_date` (`forecast_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预测记录表';

-- =============================================
-- 附加辅助表
-- =============================================

-- 消耗模式表
CREATE TABLE IF NOT EXISTS `consumption_pattern` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `reagent_id` BIGINT NOT NULL,
  `pattern_type` VARCHAR(50),
  `pattern_data` JSON,
  `confidence` DECIMAL(5,2),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_reagent` (`reagent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消耗模式表';

-- 实验活动表
CREATE TABLE IF NOT EXISTS `experiment_activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `group_id` BIGINT,
  `activity_name` VARCHAR(200),
  `start_date` DATE,
  `end_date` DATE,
  `expected_reagents` JSON,
  `status` VARCHAR(20),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_group` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验活动表';

-- 存储位置坐标表
CREATE TABLE IF NOT EXISTS `storage_location_coordinate` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `location_id` BIGINT NOT NULL,
  `coordinate_x` DECIMAL(10,2),
  `coordinate_y` DECIMAL(10,2),
  `coordinate_z` DECIMAL(10,2),
  `floor_plan_url` VARCHAR(500),
  PRIMARY KEY (`id`),
  KEY `idx_location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储位置坐标表';

-- 存储位置地图表
CREATE TABLE IF NOT EXISTS `storage_location_map` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `map_name` VARCHAR(100),
  `building` VARCHAR(100),
  `floor` VARCHAR(50),
  `map_image_url` VARCHAR(500),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储位置地图表';

-- 存储预警日志表
CREATE TABLE IF NOT EXISTS `storage_warning_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `location_id` BIGINT,
  `reagent_id` BIGINT,
  `warning_type` VARCHAR(50),
  `warning_message` TEXT,
  `severity` VARCHAR(20),
  `resolved` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_location` (`location_id`),
  KEY `idx_reagent` (`reagent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储预警日志表';

-- 扫描操作日志表
CREATE TABLE IF NOT EXISTS `scan_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT,
  `operation_type` VARCHAR(50),
  `reagent_id` BIGINT,
  `inventory_id` BIGINT,
  `scan_data` TEXT,
  `result` VARCHAR(50),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_reagent` (`reagent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫描操作日志表';

-- =============================================
-- 问题反馈表
-- =============================================
CREATE TABLE IF NOT EXISTS `feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(50) COMMENT '用户姓名',
  `feedback_type` VARCHAR(20) DEFAULT 'SYSTEM' COMMENT '反馈类型: REAGENT-试剂问题, SYSTEM-系统问题, SUGGESTION-建议',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING-待处理, PROCESSING-处理中, RESOLVED-已解决, CLOSED-已关闭',
  `handler_id` BIGINT COMMENT '处理人ID',
  `handler_name` VARCHAR(50) COMMENT '处理人姓名',
  `handle_remark` TEXT COMMENT '处理备注',
  `handle_time` DATETIME COMMENT '处理时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题反馈表';


