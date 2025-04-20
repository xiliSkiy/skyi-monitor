-- 指标定义表
CREATE TABLE IF NOT EXISTS `t_metric_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '指标定义ID',
  `code` varchar(100) NOT NULL COMMENT '指标唯一编码',
  `name` varchar(100) NOT NULL COMMENT '指标名称',
  `description` varchar(500) DEFAULT NULL COMMENT '指标描述',
  `category` varchar(50) NOT NULL COMMENT '指标类别',
  `data_type` varchar(20) NOT NULL COMMENT '数据类型',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `default_value` double DEFAULT NULL COMMENT '默认值',
  `threshold_min` double DEFAULT NULL COMMENT '最小阈值',
  `threshold_max` double DEFAULT NULL COMMENT '最大阈值',
  `collection_method` varchar(50) NOT NULL COMMENT '采集方式',
  `param_template` text DEFAULT NULL COMMENT '采集参数模板',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态(1-启用,0-禁用)',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_metric_code` (`code`),
  KEY `idx_metric_category` (`category`),
  KEY `idx_collection_method` (`collection_method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标定义表';

-- 指标协议映射表
CREATE TABLE IF NOT EXISTS `t_metric_protocol_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '映射ID',
  `metric_id` bigint(20) NOT NULL COMMENT '指标定义ID',
  `protocol` varchar(50) NOT NULL COMMENT '协议',
  `path` varchar(255) NOT NULL COMMENT '指标路径',
  `expression` text DEFAULT NULL COMMENT '解析表达式',
  `parameters` text DEFAULT NULL COMMENT '额外参数',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_metric_protocol` (`metric_id`,`protocol`),
  KEY `idx_protocol` (`protocol`),
  CONSTRAINT `fk_protocol_mapping_metric` FOREIGN KEY (`metric_id`) REFERENCES `t_metric_definition` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标协议映射表';

-- 指标资产类型映射表
CREATE TABLE IF NOT EXISTS `t_metric_asset_type_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '映射ID',
  `metric_id` bigint(20) NOT NULL COMMENT '指标定义ID',
  `asset_type` varchar(50) NOT NULL COMMENT '资产类型',
  `default_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '默认启用标志',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_metric_asset_type` (`metric_id`,`asset_type`),
  KEY `idx_asset_type` (`asset_type`),
  CONSTRAINT `fk_asset_mapping_metric` FOREIGN KEY (`metric_id`) REFERENCES `t_metric_definition` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标资产类型映射表';