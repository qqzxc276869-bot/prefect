-- =============================================
-- 试剂管理系统数据库完整初始化脚本
-- 包含建表语句和初始数据
-- =============================================

-- 删除旧数据库（如果存在）
DROP DATABASE IF EXISTS reagent_management;

-- 创建新数据库
CREATE DATABASE reagent_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
-- 10. 课题组表
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
-- 11. 课题组成员表
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
-- 12. 采购申请表
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
-- 13. GHS/MSDS信息表
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
-- 14. SOP文档表
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
-- 15. SOP培训记录表
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
-- 16. 废弃物分类表
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
-- 17. 废弃物处置记录表
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
-- 18. 试剂生命周期表
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
-- 19. 存储兼容性规则表
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
-- 20. 存储位置属性表
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
-- 21. 预测记录表
-- =============================================
CREATE TABLE IF NOT EXISTS `forecasting_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `reagent_id` BIGINT NOT NULL COMMENT '试剂ID',
  `reagent_name` VARCHAR(200) COMMENT '试剂名称',
  `current_stock` DECIMAL(10,2) COMMENT '当前库存',
  `predicted_depletion_date` DATE COMMENT '预计耗尽日期',
  `days_until_depletion` INT COMMENT '预计耗尽天数',
  `average_daily_consumption` DECIMAL(10,4) COMMENT '日均消耗量',
  `supplier_lead_time` INT COMMENT '供应商到货周期(天)',
  `recommended_order_date` DATE COMMENT '建议下单日期',
  `recommended_order_quantity` DECIMAL(10,2) COMMENT '建议订购数量',
  `confidence_level` DECIMAL(5,2) COMMENT '置信度',
  `prediction_model` VARCHAR(50) COMMENT '预测模型',
  `prediction_basis` TEXT COMMENT '预测依据(JSON)',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
  `forecast_date` DATE COMMENT '预测日期（兼容旧字段）',
  `forecast_quantity` DECIMAL(10,2) COMMENT '预测数量（兼容旧字段）',
  `forecast_period` INT COMMENT '预测周期(天)（兼容旧字段）',
  `algorithm` VARCHAR(50) COMMENT '算法（兼容旧字段）',
  `actual_quantity` DECIMAL(10,2) COMMENT '实际消耗数量（兼容旧字段）',
  `accuracy` DECIMAL(5,2) COMMENT '准确率（兼容旧字段）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_reagent` (`reagent_id`),
  KEY `idx_forecast_date` (`forecast_date`),
  KEY `idx_status` (`status`)
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


-- =============================================
-- 插入初始数据
-- =============================================

-- 清空现有数据（可选）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE budget_transaction;
TRUNCATE TABLE experiment_activity;
TRUNCATE TABLE consumption_pattern;
TRUNCATE TABLE scan_operation_log;
TRUNCATE TABLE storage_warning_log;
TRUNCATE TABLE storage_location_coordinate;
TRUNCATE TABLE storage_location_map;
TRUNCATE TABLE storage_location_attributes;
TRUNCATE TABLE storage_incompatibility_rules;
TRUNCATE TABLE reagent_lifecycle;
TRUNCATE TABLE waste_disposal_record;
TRUNCATE TABLE waste_category;
TRUNCATE TABLE sop_training_record;
TRUNCATE TABLE sop_document;
TRUNCATE TABLE ghs_msds;
TRUNCATE TABLE procurement_request;
TRUNCATE TABLE research_group_member;
TRUNCATE TABLE research_group;
TRUNCATE TABLE stock_out_record;
TRUNCATE TABLE stock_in_record;
TRUNCATE TABLE application;
TRUNCATE TABLE announcement;
TRUNCATE TABLE inventory;
TRUNCATE TABLE storage_location;
TRUNCATE TABLE reagent;
TRUNCATE TABLE reagent_category;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE forecasting_record;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 系统用户数据
-- 注意：密码使用MD5加密，123456的MD5值为：e10adc3949ba59abbe56e057f20f883e
INSERT INTO sys_user (username, password, real_name, role, email, phone, department, status, create_time) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'ADMIN', 'admin@lab.com', '13800000000', '实验室管理部', 1, NOW()),
('teacher1', 'e10adc3949ba59abbe56e057f20f883e', '张教授', 'TEACHER', 'zhang@lab.com', '13800000001', '化学系', 1, NOW()),
('teacher2', 'e10adc3949ba59abbe56e057f20f883e', '李老师', 'TEACHER', 'li@lab.com', '13800000002', '生物系', 1, NOW()),
('student1', 'e10adc3949ba59abbe56e057f20f883e', '王小明', 'STUDENT', 'wang@lab.com', '13800000003', '化学系', 1, NOW()),
('student2', 'e10adc3949ba59abbe56e057f20f883e', '刘小红', 'STUDENT', 'liu@lab.com', '13800000004', '化学系', 1, NOW()),
('student3', 'e10adc3949ba59abbe56e057f20f883e', '陈小华', 'STUDENT', 'chen@lab.com', '13800000005', '生物系', 1, NOW());

-- 2. 试剂分类数据
INSERT INTO reagent_category (name, code, description, create_time) VALUES
('有机溶剂', 'ORG_SOLVENT', '各类有机溶剂，如乙醇、甲醇、丙酮等', NOW()),
('无机酸碱', 'INORG_ACID_BASE', '无机酸和碱类试剂', NOW()),
('标准试剂', 'STANDARD', '标准物质和标准溶液', NOW()),
('生物试剂', 'BIO_REAGENT', '生物实验用试剂', NOW()),
('指示剂', 'INDICATOR', '各类指示剂', NOW()),
('金属盐类', 'METAL_SALT', '各类金属盐', NOW()),
('有机化合物', 'ORGANIC_COMPOUND', '各类有机化合物', NOW()),
('气体', 'GAS', '各类实验用气体', NOW());

-- 3. 试剂基础信息数据
INSERT INTO reagent (name, cas_no, category_id, specification, unit, manufacturer, supplier_lead_time, danger_level, description, create_time) VALUES
('乙醇', '64-17-5', 1, '分析纯AR 500ml', '瓶', '国药集团化学试剂有限公司', 3, '易燃', '无色透明液体，易挥发，可燃', NOW()),
('甲醇', '67-56-1', 1, '分析纯AR 500ml', '瓶', '国药集团化学试剂有限公司', 3, '易燃有毒', '无色透明液体，有毒', NOW()),
('丙酮', '67-64-1', 1, '分析纯AR 500ml', '瓶', '西陇科学股份有限公司', 5, '易燃', '无色透明液体，挥发性强', NOW()),
('盐酸', '7647-01-0', 2, '优级纯GR 500ml', '瓶', '国药集团化学试剂有限公司', 2, '腐蚀性', '强酸，有刺激性气味', NOW()),
('硫酸', '7664-93-9', 2, '优级纯GR 500ml', '瓶', '国药集团化学试剂有限公司', 2, '强腐蚀', '浓硫酸，强腐蚀性', NOW()),
('氢氧化钠', '1310-73-2', 2, '分析纯AR 500g', '瓶', '西陇科学股份有限公司', 3, '腐蚀性', '白色固体，强碱', NOW()),
('氯化钠', '7647-14-5', 6, '分析纯AR 500g', '瓶', '国药集团化学试剂有限公司', 3, '无危险', '白色晶体', NOW()),
('乙酸', '64-19-7', 1, '分析纯AR 500ml', '瓶', '西陇科学股份有限公司', 4, '腐蚀性', '冰醋酸，有刺激性气味', NOW()),
('葡萄糖', '50-99-7', 7, '分析纯AR 500g', '瓶', '国药集团化学试剂有限公司', 3, '无危险', '白色晶体粉末', NOW()),
('酚酞指示剂', '77-09-8', 5, '1% 乙醇溶液 100ml', '瓶', '天津市科密欧化学试剂有限公司', 5, '低危', '指示剂溶液', NOW()),
('硝酸银', '7761-88-8', 6, '分析纯AR 25g', '瓶', '上海试剂三厂', 7, '氧化性', '白色晶体，见光分解', NOW()),
('无水乙醚', '60-29-7', 1, '分析纯AR 500ml', '瓶', '国药集团化学试剂有限公司', 4, '易燃易爆', '无色透明液体，极易挥发', NOW()),
('高锰酸钾', '7722-64-7', 6, '分析纯AR 100g', '瓶', '西陇科学股份有限公司', 3, '强氧化剂', '紫黑色晶体', NOW()),
('氯仿', '67-66-3', 1, '分析纯AR 500ml', '瓶', '国药集团化学试剂有限公司', 5, '有毒', '无色透明液体，有甜味', NOW()),
('碘', '7553-56-2', 6, '分析纯AR 50g', '瓶', '上海试剂三厂', 6, '腐蚀性', '紫黑色晶体，易升华', NOW());

-- 4. 存放位置数据
INSERT INTO storage_location (room_name, cabinet_no, shelf_no, full_location, description, create_time) VALUES
('化学实验室A', 'A01', 'L1', '化学实验室A-A01-L1', '普通化学品存放区第1层', NOW()),
('化学实验室A', 'A01', 'L2', '化学实验室A-A01-L2', '普通化学品存放区第2层', NOW()),
('化学实验室A', 'A01', 'L3', '化学实验室A-A01-L3', '普通化学品存放区第3层', NOW()),
('化学实验室A', 'A02', 'L1', '化学实验室A-A02-L1', '酸碱类试剂存放区第1层', NOW()),
('化学实验室A', 'A02', 'L2', '化学实验室A-A02-L2', '酸碱类试剂存放区第2层', NOW()),
('化学实验室B', 'B01', 'L1', '化学实验室B-B01-L1', '有机溶剂存放区第1层', NOW()),
('化学实验室B', 'B01', 'L2', '化学实验室B-B01-L2', '有机溶剂存放区第2层', NOW()),
('化学实验室B', 'B02', 'L1', '化学实验室B-B02-L1', '易制毒试剂存放区', NOW()),
('生物实验室', 'C01', 'L1', '生物实验室-C01-L1', '生物试剂存放区', NOW()),
('危险品库', 'D01', 'L1', '危险品库-D01-L1', '易燃易爆品专用柜', NOW());

-- 5. 库存数据
INSERT INTO inventory (reagent_id, location_id, batch_no, quantity, warning_threshold, expiry_date, supplier, purchase_price, unit_price, purchase_date, production_date, status, remark, create_time) VALUES
(1, 6, 'ET20240101', 15.50, 5.00, '2025-12-31', '国药集团', 450.00, 30.00, '2024-01-15', '2023-12-10', 'NORMAL', '常用试剂', NOW()),
(2, 6, 'ME20240102', 8.00, 3.00, '2025-11-30', '国药集团', 400.00, 50.00, '2024-01-20', '2023-12-15', 'NORMAL', '', NOW()),
(3, 6, 'AC20240103', 12.00, 4.00, '2025-10-31', '西陇科学', 360.00, 30.00, '2024-02-01', '2024-01-10', 'NORMAL', '', NOW()),
(4, 4, 'HCL20240201', 20.00, 8.00, '2026-06-30', '国药集团', 600.00, 30.00, '2024-02-15', '2024-01-20', 'NORMAL', '浓度36-38%', NOW()),
(5, 4, 'H2SO20240202', 18.00, 6.00, '2026-12-31', '国药集团', 720.00, 40.00, '2024-02-20', '2024-01-25', 'NORMAL', '浓度95-98%', NOW()),
(6, 5, 'NAOH20240203', 25.00, 10.00, '2027-03-31', '西陇科学', 375.00, 15.00, '2024-03-01', '2024-02-10', 'NORMAL', '', NOW()),
(7, 1, 'NACL20240301', 45.00, 15.00, '2028-12-31', '国药集团', 450.00, 10.00, '2024-03-15', '2024-02-20', 'NORMAL', '', NOW()),
(8, 6, 'AC20240302', 6.00, 3.00, '2025-09-30', '西陇科学', 300.00, 50.00, '2024-03-20', '2024-02-25', 'NORMAL', '冰醋酸', NOW()),
(9, 1, 'GLU20240401', 30.00, 10.00, '2026-12-31', '国药集团', 450.00, 15.00, '2024-04-10', '2024-03-15', 'NORMAL', '', NOW()),
(10, 1, 'PHE20240402', 8.00, 2.00, '2025-12-31', '科密欧', 120.00, 15.00, '2024-04-15', '2024-03-20', 'NORMAL', '', NOW()),
(11, 2, 'AGN20240501', 2.50, 1.00, '2026-12-31', '上海试剂三厂', 500.00, 200.00, '2024-05-01', '2024-04-10', 'LOW', '贵重试剂，注意避光', NOW()),
(12, 10, 'ETH20240502', 5.00, 2.00, '2025-06-30', '国药集团', 400.00, 80.00, '2024-05-10', '2024-04-15', 'NORMAL', '易挥发，注意密封', NOW()),
(13, 2, 'KMNO20240601', 8.00, 3.00, '2027-12-31', '西陇科学', 240.00, 30.00, '2024-06-05', '2024-05-10', 'NORMAL', '', NOW()),
(14, 10, 'CHL20240602', 4.00, 2.00, '2025-08-31', '国药集团', 360.00, 90.00, '2024-06-15', '2024-05-20', 'NORMAL', '有毒，注意防护', NOW()),
(15, 2, 'IOD20240701', 1.50, 0.50, '2026-12-31', '上海试剂三厂', 300.00, 200.00, '2024-07-01', '2024-06-05', 'LOW', '易升华，密封保存', NOW());

-- 6. 课题组数据
INSERT INTO research_group (group_name, group_code, pi_id, pi_name, department, total_budget, used_budget, available_budget, budget_year, status, description, create_time) VALUES
('有机化学合成课题组', 'ORG-2024-001', 2, '张教授', '化学系', 500000.00, 125000.00, 375000.00, 2024, 'ACTIVE', '主要研究有机化合物的合成与应用', NOW()),
('分析化学课题组', 'ANA-2024-002', 2, '张教授', '化学系', 300000.00, 80000.00, 220000.00, 2024, 'ACTIVE', '专注于分析方法开发', NOW()),
('生物化学课题组', 'BIO-2024-003', 3, '李老师', '生物系', 400000.00, 150000.00, 250000.00, 2024, 'ACTIVE', '研究生物大分子相互作用', NOW());

-- 7. 课题组成员数据
INSERT INTO research_group_member (group_id, user_id, user_name, member_role, join_date, status, create_time) VALUES
(1, 2, '张教授', 'PI', '2024-01-01', 'ACTIVE', NOW()),
(1, 4, '王小明', '研究生', '2024-03-01', 'ACTIVE', NOW()),
(1, 5, '刘小红', '研究生', '2024-03-01', 'ACTIVE', NOW()),
(2, 2, '张教授', 'PI', '2024-01-01', 'ACTIVE', NOW()),
(3, 3, '李老师', 'PI', '2024-01-01', 'ACTIVE', NOW()),
(3, 6, '陈小华', '研究生', '2024-03-01', 'ACTIVE', NOW());

-- 8. 领用申请数据
INSERT INTO application (application_no, reagent_id, reagent_name, quantity, purpose, applicant_id, applicant_name, status, reviewer_id, reviewer_name, review_time, review_remark, create_time) VALUES
('APP20240601001', 1, '乙醇', 2.00, '实验室清洗和溶剂提取', 4, '王小明', 'COMPLETED', 2, '张教授', '2024-06-01 10:30:00', '同意', '2024-06-01 09:00:00'),
('APP20240601002', 3, '丙酮', 1.00, '样品萃取实验', 5, '刘小红', 'COMPLETED', 2, '张教授', '2024-06-01 14:20:00', '同意', '2024-06-01 11:00:00'),
('APP20240602001', 4, '盐酸', 0.50, 'pH调节', 6, '陈小华', 'COMPLETED', 3, '李老师', '2024-06-02 09:15:00', '同意', '2024-06-02 08:30:00'),
('APP20240603001', 7, '氯化钠', 1.00, '配制生理盐水', 6, '陈小华', 'APPROVED', 3, '李老师', '2024-06-03 10:00:00', '同意', '2024-06-03 09:00:00'),
('APP20240604001', 1, '乙醇', 1.50, '有机合成反应', 4, '王小明', 'PENDING', NULL, NULL, NULL, NULL, '2024-06-04 14:00:00');

-- 9. 系统公告数据
INSERT INTO announcement (title, content, audience, priority, creator_id, creator_name, publish_time, create_time) VALUES
('欢迎使用试剂管理系统', '欢迎各位老师和同学使用实验室试剂管理系统！\n\n系统功能包括：\n- 试剂库存查询和管理\n- 试剂申领和审批\n- 入库出库记录\n- 安全信息查询\n\n如有问题，请联系系统管理员。', 'ALL', 'INFO', 1, '系统管理员', '2024-01-01 09:00:00', '2024-01-01 09:00:00'),
('实验室安全注意事项', '各位实验室成员请注意：\n\n1. 进入实验室必须穿戴实验服、护目镜等个人防护装备\n2. 使用危险化学品前必须了解其安全特性\n3. 实验过程中严禁饮食、吸烟\n4. 实验结束后必须清理实验台面，关闭水电气源\n5. 发现安全隐患及时报告\n\n安全第一，预防为主！', 'ALL', 'WARN', 1, '系统管理员', '2024-01-15 10:00:00', '2024-01-15 10:00:00'),
('试剂申领流程说明', '各位同学请注意试剂申领流程：\n\n1. 登录系统后进入"试剂申领"页面\n2. 选择需要申领的试剂和数量\n3. 填写详细的用途说明\n4. 提交申请后等待老师审批\n5. 审批通过后到指定地点领取\n\n请提前申请，避免影响实验进度。', 'STUDENT', 'INFO', 1, '系统管理员', '2024-02-01 14:00:00', '2024-02-01 14:00:00'),
('试剂审批工作提醒', '各位老师：\n\n请及时处理学生提交的试剂申领申请。\n\n审批时请注意：\n1. 检查申请理由是否充分\n2. 确认库存是否充足\n3. 评估安全风险\n4. 及时给出审批意见\n\n建议每天至少查看一次待审批申请，避免影响学生实验进度。', 'TEACHER', 'INFO', 1, '系统管理员', '2024-02-01 14:00:00', '2024-02-01 14:00:00'),
('紧急：危险品存储检查通知', '【紧急通知】\n\n请所有实验室负责人立即检查危险品存储情况：\n\n1. 检查易燃易爆品是否存放在专用柜中\n2. 确认酸碱类试剂是否分开存放\n3. 检查通风设施是否正常\n4. 确认消防设施是否完备\n\n检查结果请于本周五前反馈给系统管理员。\n\n安全无小事，责任重于山！', 'ALL', 'URGENT', 1, '系统管理员', '2024-03-15 08:00:00', '2024-03-15 08:00:00'),
('学生实验安全培训通知', '各位同学：\n\n实验室将于本周五下午2:00在A101教室举行安全培训。\n\n培训内容：\n- 实验室安全操作规程\n- 危险化学品使用注意事项\n- 应急处理措施\n- 个人防护装备使用\n\n请所有学生准时参加，培训后将进行考核。\n\n未参加培训或考核未通过者，将暂停试剂申领权限。', 'STUDENT', 'WARN', 1, '系统管理员', '2024-03-20 10:00:00', '2024-03-20 10:00:00'),
('老师工作例会通知', '各位老师：\n\n定于本周三下午3:00在会议室召开实验室管理工作例会。\n\n会议议题：\n1. 试剂库存管理情况汇报\n2. 学生申领审批工作讨论\n3. 安全管理工作总结\n4. 下阶段工作安排\n\n请各位老师准时参加，如有特殊情况请提前请假。', 'TEACHER', 'INFO', 1, '系统管理员', '2024-04-10 09:00:00', '2024-04-10 09:00:00'),
('试剂库存预警提醒', '【重要通知】\n\n以下试剂库存已低于预警阈值，请及时采购：\n\n1. 硝酸银 - 当前库存：2.5瓶，预警阈值：1.0瓶\n2. 碘 - 当前库存：1.5瓶，预警阈值：0.5瓶\n\n请相关课题组尽快提交采购申请，避免影响实验进度。\n\n系统将定期更新库存预警信息，请及时关注。', 'ALL', 'WARN', 1, '系统管理员', '2024-05-15 11:00:00', '2024-05-15 11:00:00'),
('学生实验报告提交提醒', '各位同学：\n\n请及时提交实验报告和使用记录。\n\n要求：\n1. 实验结束后3天内提交报告\n2. 详细记录试剂使用情况\n3. 如实填写实验数据\n4. 如有异常情况需特别说明\n\n未按时提交报告者，将影响后续试剂申领。', 'STUDENT', 'INFO', 1, '系统管理员', '2024-05-20 14:00:00', '2024-05-20 14:00:00'),
('老师审批工作统计', '各位老师：\n\n本月审批工作统计：\n- 待审批申请：3件\n- 已审批申请：25件\n- 平均审批时间：1.5天\n\n感谢各位老师的辛勤工作！\n\n请继续保持高效审批，确保学生实验顺利进行。', 'TEACHER', 'INFO', 1, '系统管理员', '2024-06-01 09:00:00', '2024-06-01 09:00:00');

-- 10. 入库记录数据
INSERT INTO stock_in_record (reagent_id, inventory_id, location_id, batch_no, quantity, expiry_date, supplier, purchase_price, operator_id, operator_name, remark, create_time) VALUES
(1, 1, 6, 'ET20240101', 20.00, '2025-12-31', '国药集团', 450.00, 1, '系统管理员', '采购入库', '2024-01-15 10:00:00'),
(2, 2, 6, 'ME20240102', 10.00, '2025-11-30', '国药集团', 400.00, 1, '系统管理员', '采购入库', '2024-01-20 11:00:00'),
(3, 3, 6, 'AC20240103', 15.00, '2025-10-31', '西陇科学', 360.00, 1, '系统管理员', '采购入库', '2024-02-01 09:30:00'),
(4, 4, 4, 'HCL20240201', 25.00, '2026-06-30', '国药集团', 600.00, 1, '系统管理员', '采购入库', '2024-02-15 14:00:00'),
(5, 5, 4, 'H2SO20240202', 20.00, '2026-12-31', '国药集团', 720.00, 1, '系统管理员', '采购入库', '2024-02-20 10:30:00');

-- 11. 出库记录数据（包含历史数据用于预测分析）
INSERT INTO stock_out_record (reagent_id, inventory_id, application_id, quantity, recipient_id, recipient_name, operator_id, operator_name, purpose, remark, create_time) VALUES
-- 乙醇的历史消耗记录（最近6个月）
(1, 1, 1, 2.00, 4, '王小明', 1, '系统管理员', '实验室清洗和溶剂提取', '已领用', DATE_SUB(NOW(), INTERVAL 180 DAY)),
(1, 1, NULL, 1.50, 4, '王小明', 1, '系统管理员', '有机合成反应', '临时领用', DATE_SUB(NOW(), INTERVAL 170 DAY)),
(1, 1, NULL, 2.50, 5, '刘小红', 1, '系统管理员', '样品萃取', '临时领用', DATE_SUB(NOW(), INTERVAL 160 DAY)),
(1, 1, NULL, 1.00, 4, '王小明', 1, '系统管理员', '实验样品溶解', '临时领用', DATE_SUB(NOW(), INTERVAL 150 DAY)),
(1, 1, NULL, 3.00, 5, '刘小红', 1, '系统管理员', '溶剂提取实验', '临时领用', DATE_SUB(NOW(), INTERVAL 140 DAY)),
(1, 1, NULL, 2.00, 4, '王小明', 1, '系统管理员', '实验室清洗', '临时领用', DATE_SUB(NOW(), INTERVAL 130 DAY)),
(1, 1, NULL, 1.50, 4, '王小明', 1, '系统管理员', '有机合成反应', '临时领用', DATE_SUB(NOW(), INTERVAL 120 DAY)),
(1, 1, NULL, 2.00, 5, '刘小红', 1, '系统管理员', '样品萃取', '临时领用', DATE_SUB(NOW(), INTERVAL 110 DAY)),
(1, 1, NULL, 1.00, 4, '王小明', 1, '系统管理员', '实验样品溶解', '临时领用', DATE_SUB(NOW(), INTERVAL 100 DAY)),
(1, 1, NULL, 2.50, 5, '刘小红', 1, '系统管理员', '溶剂提取实验', '临时领用', DATE_SUB(NOW(), INTERVAL 90 DAY)),
(1, 1, NULL, 1.50, 4, '王小明', 1, '系统管理员', '实验室清洗', '临时领用', DATE_SUB(NOW(), INTERVAL 80 DAY)),
(1, 1, NULL, 2.00, 4, '王小明', 1, '系统管理员', '有机合成反应', '临时领用', DATE_SUB(NOW(), INTERVAL 70 DAY)),
(1, 1, NULL, 1.00, 5, '刘小红', 1, '系统管理员', '样品萃取', '临时领用', DATE_SUB(NOW(), INTERVAL 60 DAY)),
(1, 1, NULL, 2.50, 4, '王小明', 1, '系统管理员', '实验样品溶解', '临时领用', DATE_SUB(NOW(), INTERVAL 50 DAY)),
(1, 1, NULL, 1.50, 5, '刘小红', 1, '系统管理员', '溶剂提取实验', '临时领用', DATE_SUB(NOW(), INTERVAL 40 DAY)),
(1, 1, NULL, 2.00, 4, '王小明', 1, '系统管理员', '实验室清洗', '临时领用', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 1, NULL, 1.00, 4, '王小明', 1, '系统管理员', '有机合成反应', '临时领用', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 1, NULL, 2.50, 5, '刘小红', 1, '系统管理员', '样品萃取', '临时领用', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1, 1, NULL, 0.50, 4, '王小明', 1, '系统管理员', '实验样品溶解', '临时领用', DATE_SUB(NOW(), INTERVAL 5 DAY)),
-- 丙酮的历史消耗记录
(3, 3, 2, 1.00, 5, '刘小红', 1, '系统管理员', '样品萃取实验', '已领用', DATE_SUB(NOW(), INTERVAL 150 DAY)),
(3, 3, NULL, 1.50, 5, '刘小红', 1, '系统管理员', '样品萃取', '临时领用', DATE_SUB(NOW(), INTERVAL 120 DAY)),
(3, 3, NULL, 1.00, 5, '刘小红', 1, '系统管理员', '萃取实验', '临时领用', DATE_SUB(NOW(), INTERVAL 90 DAY)),
(3, 3, NULL, 2.00, 5, '刘小红', 1, '系统管理员', '样品萃取实验', '临时领用', DATE_SUB(NOW(), INTERVAL 60 DAY)),
(3, 3, NULL, 1.50, 5, '刘小红', 1, '系统管理员', '萃取实验', '临时领用', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(3, 3, NULL, 1.00, 5, '刘小红', 1, '系统管理员', '样品萃取', '临时领用', DATE_SUB(NOW(), INTERVAL 10 DAY)),
-- 盐酸的历史消耗记录
(4, 4, 3, 0.50, 6, '陈小华', 1, '系统管理员', 'pH调节', '已领用', DATE_SUB(NOW(), INTERVAL 140 DAY)),
(4, 4, NULL, 0.30, 6, '陈小华', 1, '系统管理员', 'pH调节', '临时领用', DATE_SUB(NOW(), INTERVAL 110 DAY)),
(4, 4, NULL, 0.50, 6, '陈小华', 1, '系统管理员', 'pH调节', '临时领用', DATE_SUB(NOW(), INTERVAL 80 DAY)),
(4, 4, NULL, 0.40, 6, '陈小华', 1, '系统管理员', 'pH调节', '临时领用', DATE_SUB(NOW(), INTERVAL 50 DAY)),
(4, 4, NULL, 0.50, 6, '陈小华', 1, '系统管理员', 'pH调节', '临时领用', DATE_SUB(NOW(), INTERVAL 20 DAY)),
-- 氯化钠的历史消耗记录
(7, 7, NULL, 2.00, 6, '陈小华', 1, '系统管理员', '配制缓冲液', '临时领用', DATE_SUB(NOW(), INTERVAL 130 DAY)),
(7, 7, NULL, 1.50, 6, '陈小华', 1, '系统管理员', '配制缓冲液', '临时领用', DATE_SUB(NOW(), INTERVAL 100 DAY)),
(7, 7, NULL, 2.00, 6, '陈小华', 1, '系统管理员', '配制生理盐水', '临时领用', DATE_SUB(NOW(), INTERVAL 70 DAY)),
(7, 7, NULL, 1.50, 6, '陈小华', 1, '系统管理员', '配制缓冲液', '临时领用', DATE_SUB(NOW(), INTERVAL 40 DAY)),
(7, 7, NULL, 2.00, 6, '陈小华', 1, '系统管理员', '配制生理盐水', '临时领用', DATE_SUB(NOW(), INTERVAL 10 DAY));

-- 12. 采购申请数据
INSERT INTO procurement_request (request_no, reagent_id, reagent_name, quantity, unit, estimated_price, supplier, requester_id, requester_name, group_id, urgency, reason, status, approver_id, approver_name, approval_time, approval_remark, create_time) VALUES
('PRO20240501001', 1, '乙醇', 20.00, '瓶', 600.00, '国药集团', 4, '王小明', 1, 'NORMAL', '实验室常用溶剂即将用完', 'RECEIVED', 2, '张教授', '2024-05-02 09:00:00', '同意采购', '2024-05-01 14:00:00'),
('PRO20240515001', 11, '硝酸银', 5.00, '瓶', 1000.00, '上海试剂三厂', 5, '刘小红', 1, 'HIGH', '课题实验急需', 'APPROVED', 2, '张教授', '2024-05-16 10:00:00', '同意，尽快订购', '2024-05-15 16:00:00'),
('PRO20240601001', 9, '葡萄糖', 10.00, '瓶', 150.00, '国药集团', 6, '陈小华', 3, 'NORMAL', '生物实验补充试剂', 'PENDING', NULL, NULL, NULL, NULL, '2024-06-01 10:00:00');

-- 13. GHS/MSDS信息数据
INSERT INTO ghs_msds (reagent_id, cas_no, reagent_name, ghs_classification, signal_word, hazard_statements, precautionary_statements, first_aid, fire_fighting, spill_handling, storage_conditions, disposal_methods, create_time) VALUES
(1, '64-17-5', '乙醇', '易燃液体类别2', '危险', 'H225:高度易燃液体和蒸气', 'P210:远离热源、火花、明火、热表面。禁止吸烟；P233:保持容器密闭；P240:容器和接收设备接地/等电位连接', '吸入：迅速脱离现场至空气新鲜处。保持呼吸道通畅。如呼吸困难，给输氧。如呼吸停止，立即进行人工呼吸。就医。', '用雾状水、泡沫、干粉、二氧化碳灭火。', '迅速撤离泄漏污染区人员至安全区，并进行隔离，严格限制出入。切断火源。建议应急处理人员戴自给正压式呼吸器，穿防静电工作服。', '储存于阴凉、通风的库房。远离火种、热源。库温不宜超过30℃。保持容器密封。应与氧化剂、酸类、碱金属、胺类等分开存放。', '建议用焚烧法处置。', NOW()),
(2, '67-56-1', '甲醇', '易燃液体类别2；急性毒性类别3（经口、经皮、吸入）', '危险', 'H225:高度易燃液体和蒸气；H301:吞咽会中毒；H311:皮肤接触会中毒；H331:吸入会中毒', 'P210:远离热源、火花、明火、热表面。禁止吸烟；P280:戴防护手套/穿防护服/戴防护眼镜/戴防护面具', '皮肤接触：脱去污染的衣着，用肥皂水和清水彻底冲洗皮肤。眼睛接触：提起眼睑，用流动清水或生理盐水冲洗。就医。吸入：迅速脱离现场至空气新鲜处。', '用雾状水、抗溶性泡沫、干粉、二氧化碳灭火。', '迅速撤离泄漏污染区人员至安全区，并进行隔离，严格限制出入。切断火源。', '储存于阴凉、通风的库房。远离火种、热源。库温不宜超过37℃。保持容器密封。应与氧化剂、酸类、碱金属等分开存放。', '建议用焚烧法处置。焚烧炉排出的气体要通过洗涤器除去。', NOW()),
(4, '7647-01-0', '盐酸', '金属腐蚀物类别1；皮肤腐蚀/刺激类别1B', '危险', 'H290:可能腐蚀金属；H314:造成严重皮肤灼伤和眼损伤', 'P280:戴防护手套/穿防护服/戴防护眼镜/戴防护面具；P303+P361+P353:如皮肤（或头发）沾染：立即脱掉/脱去所有沾染的衣服。用水清洗皮肤/淋浴', '皮肤接触：立即脱去污染的衣着，用大量流动清水冲洗至少15分钟。就医。眼睛接触：立即提起眼睑，用大量流动清水或生理盐水彻底冲洗至少15分钟。就医。', '用碱性物质如碳酸氢钠、碳酸钠、消石灰等中和。也可用大量水扑救。', '迅速撤离泄漏污染区人员至安全区，并进行隔离，严格限制出入。建议应急处理人员戴自给正压式呼吸器，穿防酸碱工作服。', '储存于阴凉、通风的库房。库温不超过30℃，相对湿度不超过85％。保持容器密封。应与碱类、胺类、碱金属、易（可）燃物分开存放。', '用碱液中和后，用大量水冲洗，经稀释的污水放入废水系统。', NOW()),
(5, '7664-93-9', '硫酸', '金属腐蚀物类别1；皮肤腐蚀/刺激类别1A', '危险', 'H290:可能腐蚀金属；H314:造成严重皮肤灼伤和眼损伤', 'P280:戴防护手套/穿防护服/戴防护眼镜/戴防护面具；P301+P330+P331:如误吞咽：漱口。不要诱导呕吐', '皮肤接触：立即脱去污染的衣着，用大量流动清水冲洗至少15分钟。就医。眼睛接触：立即提起眼睑，用大量流动清水或生理盐水彻底冲洗至少15分钟。就医。', '消防人员必须穿全身耐酸碱消防服。灭火剂：干粉、二氧化碳、砂土。避免水流冲击物品，以免遇水会放出大量热量发生喷溅而灼伤皮肤。', '迅速撤离泄漏污染区人员至安全区，并进行隔离，严格限制出入。建议应急处理人员戴自给正压式呼吸器，穿防酸碱工作服。', '储存于阴凉、通风的库房。库温不超过35℃，相对湿度不超过85％。保持容器密封。应与易（可）燃物、碱类、金属粉末等分开存放。', '用碱液中和后，用大量水冲洗，经稀释的污水放入废水系统。', NOW());

-- 14. SOP文档数据
INSERT INTO sop_document (title, sop_no, category, content, version, status, author_id, author_name, reviewer_id, reviewer_name, publish_time, create_time) VALUES
('实验室安全操作规程', 'SOP-2024-001', '安全管理', '1. 目的：规范实验室安全操作，确保人员和设备安全。\n2. 范围：适用于所有实验室工作人员。\n3. 内容：\n3.1 进入实验室必须穿戴实验服、护目镜等个人防护装备。\n3.2 使用危险化学品前必须了解其安全特性。\n3.3 实验过程中严禁饮食、吸烟。\n3.4 实验结束后必须清理实验台面，关闭水电气源。', 'V1.0', 'PUBLISHED', 1, '系统管理员', 2, '张教授', '2024-01-15 10:00:00', '2024-01-10 14:00:00'),
('试剂入库操作规程', 'SOP-2024-002', '库存管理', '1. 目的：规范试剂入库流程，确保库存准确。\n2. 范围：适用于所有试剂入库操作。\n3. 内容：\n3.1 验收试剂包装完整性，核对品名、规格、数量。\n3.2 检查生产日期、有效期，拒收过期或临期试剂。\n3.3 在系统中录入试剂信息，打印标签并粘贴。\n3.4 按照分类和存储要求放置到指定位置。', 'V1.0', 'PUBLISHED', 1, '系统管理员', 2, '张教授', '2024-01-20 11:00:00', '2024-01-15 09:00:00'),
('危险化学品应急处理规程', 'SOP-2024-003', '应急管理', '1. 目的：规范危险化学品事故应急处理，减少损失。\n2. 范围：适用于所有危险化学品泄漏、火灾等事故。\n3. 内容：\n3.1 发现事故立即报警并通知相关人员。\n3.2 疏散无关人员，隔离事故区域。\n3.3 根据化学品特性选择合适的应急处理方法。\n3.4 事故处理后进行总结和记录。', 'V1.0', 'PUBLISHED', 1, '系统管理员', 2, '张教授', '2024-02-01 10:00:00', '2024-01-25 15:00:00');

-- 15. SOP培训记录数据
INSERT INTO sop_training_record (sop_id, user_id, user_name, training_date, trainer_id, trainer_name, score, status, remark, create_time) VALUES
(1, 4, '王小明', '2024-03-01', 2, '张教授', 92.00, 'PASSED', '认真学习，掌握良好', NOW()),
(1, 5, '刘小红', '2024-03-01', 2, '张教授', 88.00, 'PASSED', '基本掌握', NOW()),
(1, 6, '陈小华', '2024-03-01', 3, '李老师', 95.00, 'PASSED', '优秀', NOW()),
(2, 4, '王小明', '2024-03-15', 2, '张教授', 90.00, 'PASSED', '', NOW()),
(2, 5, '刘小红', '2024-03-15', 2, '张教授', 85.00, 'PASSED', '', NOW());

-- 16. 废弃物分类数据
INSERT INTO waste_category (name, code, description, disposal_method, create_time) VALUES
('有机废液', 'WASTE-ORG-LIQUID', '各类有机溶剂废液', '收集后统一交由有资质的危废处置单位处理', NOW()),
('无机废液', 'WASTE-INORG-LIQUID', '无机酸碱盐废液', '中和后达标排放或交由危废处置单位处理', NOW()),
('固体废物', 'WASTE-SOLID', '实验产生的固体废弃物', '分类收集后交由危废处置单位处理', NOW()),
('重金属废物', 'WASTE-HEAVY-METAL', '含重金属的废液或固体', '收集后交由危废处置单位特殊处理', NOW());

-- 17. 废弃物处置记录数据
INSERT INTO waste_disposal_record (record_no, waste_category_id, waste_name, quantity, unit, source_location, disposal_method, disposal_date, handler_id, handler_name, status, remark, create_time) VALUES
('WDR20240401001', 1, '乙醇废液', 5.00, 'L', '化学实验室A', '交由危废处置单位处理', '2024-04-15', 1, '系统管理员', 'COMPLETED', '已按规定处理', '2024-04-01 10:00:00'),
('WDR20240501001', 2, '酸性废液', 10.00, 'L', '化学实验室A', '中和后达标排放', '2024-05-10', 1, '系统管理员', 'COMPLETED', 'pH中和至7，检测达标', '2024-05-01 14:00:00'),
('WDR20240601001', 3, '实验固废', 2.00, 'kg', '化学实验室B', '交由危废处置单位处理', NULL, 1, '系统管理员', 'PENDING', '待处理', '2024-06-01 09:00:00');

-- 18. 试剂生命周期记录数据
INSERT INTO reagent_lifecycle (reagent_id, inventory_id, batch_no, lifecycle_stage, event_type, event_description, operator_id, operator_name, event_time) VALUES
(1, 1, 'ET20240101', 'PROCUREMENT', '采购', '采购20瓶乙醇，批次号ET20240101', 1, '系统管理员', '2024-01-15 10:00:00'),
(1, 1, 'ET20240101', 'INBOUND', '入库', '试剂入库，存放位置：化学实验室B-B01-L1', 1, '系统管理员', '2024-01-15 14:00:00'),
(1, 1, 'ET20240101', 'OUTBOUND', '出库', '王小明领用2瓶，用途：实验室清洗和溶剂提取', 1, '系统管理员', '2024-06-01 11:00:00'),
(1, 1, 'ET20240101', 'OUTBOUND', '出库', '王小明领用0.5瓶，用途：实验样品溶解', 1, '系统管理员', '2024-06-05 09:30:00'),
(4, 4, 'HCL20240201', 'PROCUREMENT', '采购', '采购25瓶盐酸，批次号HCL20240201', 1, '系统管理员', '2024-02-15 14:00:00'),
(4, 4, 'HCL20240201', 'INBOUND', '入库', '试剂入库，存放位置：化学实验室A-A02-L1', 1, '系统管理员', '2024-02-15 16:00:00'),
(4, 4, 'HCL20240201', 'OUTBOUND', '出库', '陈小华领用0.5瓶，用途：pH调节', 1, '系统管理员', '2024-06-02 10:00:00');

-- 19. 存储兼容性规则数据
INSERT INTO storage_incompatibility_rules (hazard_type1, hazard_type2, incompatibility_level, description, safety_distance, enabled, create_time) VALUES
('易燃液体', '强酸', 'HIGH', '有机溶剂与强酸反应可能引发火灾或爆炸', 2.00, 1, NOW()),
('易燃液体', '强碱', 'HIGH', '有机溶剂与强碱反应可能引发火灾', 2.00, 1, NOW()),
('易燃液体', '氧化剂', 'MEDIUM', '易燃液体与氧化剂接触可能发生氧化反应', 1.50, 1, NOW()),
('强酸', '强碱', 'HIGH', '酸碱中和反应剧烈，可能产生大量热量', 1.50, 1, NOW()),
('酸类', '金属盐', 'MEDIUM', '酸与活泼金属盐反应可能产生有毒气体', 1.00, 1, NOW());

-- 20. 存储位置属性数据
INSERT INTO storage_location_attributes (location_id, is_ventilated, is_explosion_proof, temperature_control, humidity_control, max_capacity, special_requirements, create_time) VALUES
(6, 1, 1, '15-25℃', '相对湿度<60%', 100.00, '易燃品专用柜，配备防爆设施', NOW()),
(4, 0, 0, '室温', '相对湿度<70%', 150.00, '防腐蚀材质', NOW()),
(10, 1, 1, '15-20℃', '相对湿度<50%', 50.00, '危险品专用库，24小时监控', NOW());

-- 21. 预算交易记录数据
INSERT INTO budget_transaction (group_id, transaction_type, amount, description, create_time) VALUES
(1, 'ALLOCATION', 500000.00, '2024年度预算分配', '2024-01-01 00:00:00'),
(1, 'EXPENSE', -25000.00, '试剂采购支出', '2024-02-01 10:00:00'),
(1, 'EXPENSE', -50000.00, '设备购置支出', '2024-03-15 14:00:00'),
(1, 'EXPENSE', -30000.00, '试剂采购支出', '2024-04-20 11:00:00'),
(1, 'EXPENSE', -20000.00, '耗材采购支出', '2024-05-10 15:00:00'),
(2, 'ALLOCATION', 300000.00, '2024年度预算分配', '2024-01-01 00:00:00'),
(2, 'EXPENSE', -40000.00, '仪器维护费用', '2024-02-15 09:00:00'),
(2, 'EXPENSE', -40000.00, '试剂采购支出', '2024-04-01 10:00:00'),
(3, 'ALLOCATION', 400000.00, '2024年度预算分配', '2024-01-01 00:00:00'),
(3, 'EXPENSE', -80000.00, '生物试剂采购', '2024-03-01 14:00:00'),
(3, 'EXPENSE', -70000.00, '实验动物购置', '2024-05-15 11:00:00');

-- =============================================
-- 数据库更新部分（用于更新现有数据库）
-- 如果数据库已存在，执行以下语句来添加缺失的字段
-- =============================================

-- 更新 forecasting_record 表结构（如果字段不存在则添加）
DELIMITER $$

DROP PROCEDURE IF EXISTS `AddColumnIfNotExists`$$

CREATE PROCEDURE `AddColumnIfNotExists`(
    IN tableName VARCHAR(128),
    IN columnName VARCHAR(128),
    IN columnDefinition TEXT
)
BEGIN
    DECLARE columnExists INT DEFAULT 0;
    
    SELECT COUNT(*) INTO columnExists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = tableName
      AND COLUMN_NAME = columnName;
    
    IF columnExists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', tableName, '` ADD COLUMN `', columnName, '` ', columnDefinition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

-- 更新 forecasting_record 表，添加缺失的字段
CALL AddColumnIfNotExists('forecasting_record', 'reagent_name', 'VARCHAR(200) COMMENT ''试剂名称'' AFTER `reagent_id`');
CALL AddColumnIfNotExists('forecasting_record', 'current_stock', 'DECIMAL(10,2) COMMENT ''当前库存'' AFTER `reagent_name`');
CALL AddColumnIfNotExists('forecasting_record', 'predicted_depletion_date', 'DATE COMMENT ''预计耗尽日期'' AFTER `current_stock`');
CALL AddColumnIfNotExists('forecasting_record', 'days_until_depletion', 'INT COMMENT ''预计耗尽天数'' AFTER `predicted_depletion_date`');
CALL AddColumnIfNotExists('forecasting_record', 'average_daily_consumption', 'DECIMAL(10,4) COMMENT ''日均消耗量'' AFTER `days_until_depletion`');
CALL AddColumnIfNotExists('forecasting_record', 'supplier_lead_time', 'INT COMMENT ''供应商到货周期(天)'' AFTER `average_daily_consumption`');
CALL AddColumnIfNotExists('forecasting_record', 'recommended_order_date', 'DATE COMMENT ''建议下单日期'' AFTER `supplier_lead_time`');
CALL AddColumnIfNotExists('forecasting_record', 'recommended_order_quantity', 'DECIMAL(10,2) COMMENT ''建议订购数量'' AFTER `recommended_order_date`');
CALL AddColumnIfNotExists('forecasting_record', 'prediction_model', 'VARCHAR(50) COMMENT ''预测模型'' AFTER `confidence_level`');
CALL AddColumnIfNotExists('forecasting_record', 'prediction_basis', 'TEXT COMMENT ''预测依据(JSON)'' AFTER `prediction_model`');
CALL AddColumnIfNotExists('forecasting_record', 'status', 'VARCHAR(20) DEFAULT ''ACTIVE'' COMMENT ''状态'' AFTER `prediction_basis`');
CALL AddColumnIfNotExists('forecasting_record', 'update_time', 'DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'' AFTER `create_time`');

-- 添加索引（如果不存在）
SET @indexExists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'forecasting_record'
      AND INDEX_NAME = 'idx_status'
);

SET @sql = IF(@indexExists = 0,
    'ALTER TABLE `forecasting_record` ADD INDEX `idx_status` (`status`)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 清理存储过程
DROP PROCEDURE IF EXISTS `AddColumnIfNotExists`;

-- =============================================
-- 数据库初始化完成
-- =============================================

