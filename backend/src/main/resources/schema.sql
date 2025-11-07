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
-- 9. 课题组表
-- =============================================
CREATE TABLE IF NOT EXISTS `research_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课题组ID',
  `group_name` VARCHAR(200) NOT NULL COMMENT '课题组名称',
  `group_code` VARCHAR(50) COMMENT '课题组编码',
  `pi_id` BIGINT COMMENT '负责人ID',
  `pi_name` VARCHAR(50) COMMENT '负责人姓名',
  `department` VARCHAR(100) COMMENT '所属部门',
  `total_budget` DECIMAL(12,2) DEFAULT 0 COMMENT '总预算',
  `used_budget` DECIMAL(12,2) DEFAULT 0 COMMENT '已用预算',
  `available_budget` DECIMAL(12,2) DEFAULT 0 COMMENT '可用预算',
  `budget_year` INT COMMENT '预算年份',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
  `description` TEXT COMMENT '描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_code` (`group_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课题组表';

-- =============================================
-- 10. 课题组成员表
-- =============================================
CREATE TABLE IF NOT EXISTS `research_group_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `group_id` BIGINT NOT NULL COMMENT '课题组ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(50) COMMENT '用户姓名',
  `member_role` VARCHAR(50) COMMENT '成员角色',
  `join_date` DATE COMMENT '加入日期',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_group` (`group_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课题组成员表';

-- =============================================
-- 11. 采购申请表
-- =============================================
CREATE TABLE IF NOT EXISTS `procurement_request` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '采购ID',
  `request_no` VARCHAR(50) NOT NULL COMMENT '采购单号',
  `reagent_id` BIGINT COMMENT '试剂ID',
  `reagent_name` VARCHAR(200) COMMENT '试剂名称',
  `quantity` DECIMAL(10,2) NOT NULL COMMENT '采购数量',
  `unit` VARCHAR(20) COMMENT '单位',
  `estimated_price` DECIMAL(10,2) COMMENT '预估价格',
  `supplier` VARCHAR(200) COMMENT '供应商',
  `requester_id` BIGINT COMMENT '申请人ID',
  `requester_name` VARCHAR(50) COMMENT '申请人姓名',
  `group_id` BIGINT COMMENT '课题组ID',
  `urgency` VARCHAR(20) COMMENT '紧急程度',
  `reason` TEXT COMMENT '采购原因',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝, ORDERED-已订购, RECEIVED-已收货',
  `approver_id` BIGINT COMMENT '审批人ID',
  `approver_name` VARCHAR(50) COMMENT '审批人姓名',
  `approval_time` DATETIME COMMENT '审批时间',
  `approval_remark` TEXT COMMENT '审批备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_request_no` (`request_no`),
  KEY `idx_reagent` (`reagent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购申请表';

-- =============================================
-- 12. GHS/MSDS信息表
-- =============================================
CREATE TABLE IF NOT EXISTS `ghs_msds` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `reagent_id` BIGINT NOT NULL COMMENT '试剂ID',
  `cas_no` VARCHAR(50) COMMENT 'CAS号',
  `reagent_name` VARCHAR(200) COMMENT '试剂名称',
  `ghs_classification` TEXT COMMENT 'GHS分类',
  `signal_word` VARCHAR(50) COMMENT '信号词',
  `hazard_statements` TEXT COMMENT '危险性说明',
  `precautionary_statements` TEXT COMMENT '防范说明',
  `first_aid` TEXT COMMENT '急救措施',
  `fire_fighting` TEXT COMMENT '消防措施',
  `spill_handling` TEXT COMMENT '泄露应急处理',
  `storage_conditions` TEXT COMMENT '储存条件',
  `disposal_methods` TEXT COMMENT '废弃处置',
  `msds_file_url` VARCHAR(500) COMMENT 'MSDS文件URL',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_reagent` (`reagent_id`),
  KEY `idx_cas_no` (`cas_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='GHS/MSDS信息表';

-- =============================================
-- 13. SOP文档表
-- =============================================
CREATE TABLE IF NOT EXISTS `sop_document` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'SOP ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `sop_no` VARCHAR(50) COMMENT 'SOP编号',
  `category` VARCHAR(100) COMMENT '分类',
  `content` LONGTEXT COMMENT '内容',
  `version` VARCHAR(20) COMMENT '版本号',
  `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT-草稿, PUBLISHED-已发布, ARCHIVED-已归档',
  `author_id` BIGINT COMMENT '作者ID',
  `author_name` VARCHAR(50) COMMENT '作者姓名',
  `reviewer_id` BIGINT COMMENT '审核人ID',
  `reviewer_name` VARCHAR(50) COMMENT '审核人姓名',
  `publish_time` DATETIME COMMENT '发布时间',
  `file_url` VARCHAR(500) COMMENT '文件URL',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sop_no` (`sop_no`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SOP文档表';

-- =============================================
-- 14. SOP培训记录表
-- =============================================
CREATE TABLE IF NOT EXISTS `sop_training_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `sop_id` BIGINT NOT NULL COMMENT 'SOP ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(50) COMMENT '用户姓名',
  `training_date` DATE COMMENT '培训日期',
  `trainer_id` BIGINT COMMENT '培训师ID',
  `trainer_name` VARCHAR(50) COMMENT '培训师姓名',
  `score` DECIMAL(5,2) COMMENT '考核分数',
  `status` VARCHAR(20) COMMENT '状态: PASSED-通过, FAILED-未通过',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_sop` (`sop_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SOP培训记录表';

-- =============================================
-- 15. 废弃物分类表
-- =============================================
CREATE TABLE IF NOT EXISTS `waste_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `code` VARCHAR(50) COMMENT '分类编码',
  `description` TEXT COMMENT '描述',
  `disposal_method` TEXT COMMENT '处置方法',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='废弃物分类表';

-- =============================================
-- 16. 废弃物处置记录表
-- =============================================
CREATE TABLE IF NOT EXISTS `waste_disposal_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `record_no` VARCHAR(50) NOT NULL COMMENT '记录编号',
  `waste_category_id` BIGINT COMMENT '废弃物分类ID',
  `waste_name` VARCHAR(200) COMMENT '废弃物名称',
  `quantity` DECIMAL(10,2) COMMENT '数量',
  `unit` VARCHAR(20) COMMENT '单位',
  `source_location` VARCHAR(200) COMMENT '来源位置',
  `disposal_method` VARCHAR(200) COMMENT '处置方法',
  `disposal_date` DATE COMMENT '处置日期',
  `handler_id` BIGINT COMMENT '处理人ID',
  `handler_name` VARCHAR(50) COMMENT '处理人姓名',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING-待处置, PROCESSING-处置中, COMPLETED-已完成',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`),
  KEY `idx_category` (`waste_category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='废弃物处置记录表';

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
-- 18. 存储兼容性规则表
-- =============================================
CREATE TABLE IF NOT EXISTS `storage_incompatibility_rules` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `hazard_type1` VARCHAR(100) NOT NULL COMMENT '危险品类型1',
  `hazard_type2` VARCHAR(100) NOT NULL COMMENT '危险品类型2',
  `incompatibility_level` VARCHAR(20) COMMENT '不兼容等级',
  `description` TEXT COMMENT '描述',
  `safety_distance` DECIMAL(10,2) COMMENT '安全距离(米)',
  `enabled` INT DEFAULT 1 COMMENT '是否启用: 0-否, 1-是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_hazard_type1` (`hazard_type1`),
  KEY `idx_hazard_type2` (`hazard_type2`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储兼容性规则表';

-- =============================================
-- 19. 存储位置属性表
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

-- 预算交易表
CREATE TABLE IF NOT EXISTS `budget_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `group_id` BIGINT NOT NULL,
  `transaction_type` VARCHAR(50),
  `amount` DECIMAL(12,2),
  `related_id` BIGINT,
  `related_type` VARCHAR(50),
  `balance_before` DECIMAL(12,2),
  `balance_after` DECIMAL(12,2),
  `reagent_name` VARCHAR(200),
  `quantity` DECIMAL(10,2),
  `description` TEXT,
  `operator_id` BIGINT,
  `operator_name` VARCHAR(50),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_group` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算交易表';

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

-- 实验模式表
CREATE TABLE IF NOT EXISTS `experiment_pattern` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `group_id` BIGINT,
  `pattern_name` VARCHAR(100),
  `reagent_list` JSON,
  `frequency` VARCHAR(50),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_group` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验模式表';

