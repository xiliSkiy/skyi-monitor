-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `skyi_storage` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `skyi_storage`;

-- 创建元数据表
CREATE TABLE IF NOT EXISTS `metadata` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` varchar(50) NOT NULL COMMENT '元数据类型',
  `ref_id` varchar(100) NOT NULL COMMENT '关联ID',
  `meta_key` varchar(100) NOT NULL COMMENT '元数据键',
  `meta_value` text COMMENT '元数据值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type_ref_id` (`type`, `ref_id`),
  KEY `idx_meta_key` (`meta_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='元数据表';

-- 创建常用的元数据类型记录（示例数据）
INSERT INTO `metadata` (`type`, `ref_id`, `meta_key`, `meta_value`) VALUES
('system', 'storage', 'version', '1.0.0'),
('system', 'storage', 'initialized', 'true'),
('system', 'influxdb', 'default_bucket', 'monitoring'),
('system', 'influxdb', 'default_org', 'skyi');

-- 创建时序数据索引表（可选，用于加速查询）
CREATE TABLE IF NOT EXISTS `time_series_index` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `asset_id` varchar(100) NOT NULL COMMENT '资产ID',
  `metric_name` varchar(100) NOT NULL COMMENT '指标名称',
  `first_timestamp` datetime NOT NULL COMMENT '首次记录时间',
  `last_timestamp` datetime NOT NULL COMMENT '最后记录时间',
  `sample_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '样本数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_metric` (`asset_id`, `metric_name`),
  KEY `idx_metric_name` (`metric_name`),
  KEY `idx_asset_id` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时序数据索引表';

-- 权限授予（如果需要）
-- GRANT ALL PRIVILEGES ON `skyi_storage`.* TO 'skyi_user'@'%' IDENTIFIED BY 'skyi_password';
-- FLUSH PRIVILEGES; 