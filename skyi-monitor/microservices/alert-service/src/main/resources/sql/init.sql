-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `skyi_alert` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `skyi_alert`;

-- 创建告警定义表
CREATE TABLE IF NOT EXISTS `alert_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '告警名称',
  `description` varchar(500) DEFAULT NULL COMMENT '告警描述',
  `expression` text NOT NULL COMMENT '告警表达式',
  `severity` varchar(20) NOT NULL COMMENT '严重程度',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `resource_type` varchar(50) DEFAULT NULL COMMENT '资源类型',
  `category` varchar(50) DEFAULT NULL COMMENT '告警类别',
  `notification_template` text DEFAULT NULL COMMENT '通知模板',
  `auto_resolve` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否自动解决',
  `recovery_expression` text DEFAULT NULL COMMENT '恢复表达式',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_severity` (`severity`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警定义表';

-- 创建告警记录表
CREATE TABLE IF NOT EXISTS `alert_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `definition_id` bigint(20) NOT NULL COMMENT '告警定义ID',
  `name` varchar(100) NOT NULL COMMENT '告警名称',
  `severity` varchar(20) NOT NULL COMMENT '严重程度',
  `resource_type` varchar(50) DEFAULT NULL COMMENT '资源类型',
  `resource_id` varchar(100) DEFAULT NULL COMMENT '资源ID',
  `resource_name` varchar(100) DEFAULT NULL COMMENT '资源名称',
  `metric_name` varchar(100) DEFAULT NULL COMMENT '指标名称',
  `threshold` double DEFAULT NULL COMMENT '阈值',
  `actual_value` double DEFAULT NULL COMMENT '实际值',
  `message` text DEFAULT NULL COMMENT '告警消息',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `acknowledged` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已确认',
  `acknowledged_by` varchar(50) DEFAULT NULL COMMENT '确认人',
  `acknowledged_time` datetime DEFAULT NULL COMMENT '确认时间',
  `acknowledged_comment` text DEFAULT NULL COMMENT '确认备注',
  PRIMARY KEY (`id`),
  KEY `idx_definition_id` (`definition_id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_status` (`status`),
  KEY `idx_severity` (`severity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表';

-- 创建告警通知记录表
CREATE TABLE IF NOT EXISTS `alert_notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `alert_id` bigint(20) NOT NULL COMMENT '告警记录ID',
  `channel` varchar(20) NOT NULL COMMENT '通知渠道',
  `recipient` varchar(200) NOT NULL COMMENT '接收者',
  `content` text NOT NULL COMMENT '通知内容',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `error_message` text DEFAULT NULL COMMENT '错误信息',
  `send_time` datetime NOT NULL COMMENT '发送时间',
  `retry_count` int(11) NOT NULL DEFAULT 0 COMMENT '重试次数',
  `last_retry_time` datetime DEFAULT NULL COMMENT '最后重试时间',
  PRIMARY KEY (`id`),
  KEY `idx_alert_id` (`alert_id`),
  KEY `idx_send_time` (`send_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警通知记录表';

-- 创建用户告警配置表
CREATE TABLE IF NOT EXISTS `user_alert_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `channels` varchar(255) NOT NULL COMMENT '通知渠道',
  `silence_start` time DEFAULT NULL COMMENT '免打扰开始时间',
  `silence_end` time DEFAULT NULL COMMENT '免打扰结束时间',
  `severity_filter` varchar(100) DEFAULT NULL COMMENT '严重程度过滤器',
  `category_filter` varchar(255) DEFAULT NULL COMMENT '类别过滤器',
  `resource_filter` varchar(255) DEFAULT NULL COMMENT '资源过滤器',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户告警配置表';

-- 插入示例数据
INSERT INTO `alert_definition` (`name`, `description`, `expression`, `severity`, `resource_type`, `category`, `notification_template`, `auto_resolve`, `recovery_expression`) VALUES
('CPU使用率过高', 'CPU使用率超过阈值', 'system.cpu.usage > 80', 'WARNING', 'HOST', '性能', '主机 {{resource_name}} CPU使用率达到 {{actual_value}}%，超过阈值 {{threshold}}%', 1, 'system.cpu.usage <= 70'),
('内存使用率过高', '内存使用率超过阈值', 'system.memory.usage > 90', 'WARNING', 'HOST', '性能', '主机 {{resource_name}} 内存使用率达到 {{actual_value}}%，超过阈值 {{threshold}}%', 1, 'system.memory.usage <= 80'),
('磁盘空间不足', '磁盘空间使用率超过阈值', 'system.disk.usage > 90', 'CRITICAL', 'HOST', '容量', '主机 {{resource_name}} 磁盘空间使用率达到 {{actual_value}}%，超过阈值 {{threshold}}%', 1, 'system.disk.usage <= 80'),
('服务响应超时', '服务响应时间超过阈值', 'http.server.requests > 5000', 'CRITICAL', 'SERVICE', '可用性', '服务 {{resource_name}} 响应时间达到 {{actual_value}}ms，超过阈值 {{threshold}}ms', 1, 'http.server.requests <= 3000'),
('API错误率过高', 'API错误率超过阈值', 'api.error.rate > 5', 'WARNING', 'SERVICE', '可用性', '服务 {{resource_name}} API错误率达到 {{actual_value}}%，超过阈值 {{threshold}}%', 1, 'api.error.rate <= 2');

-- 示例告警记录
INSERT INTO `alert_record` (`definition_id`, `name`, `severity`, `resource_type`, `resource_id`, `resource_name`, `metric_name`, `threshold`, `actual_value`, `message`, `start_time`, `status`) VALUES
(1, 'CPU使用率过高', 'WARNING', 'HOST', 'host-001', 'web-server-01', 'system.cpu.usage', 80, 85.6, '主机 web-server-01 CPU使用率达到 85.6%，超过阈值 80%', NOW(), 'ACTIVE'),
(2, '内存使用率过高', 'WARNING', 'HOST', 'host-002', 'app-server-02', 'system.memory.usage', 90, 93.2, '主机 app-server-02 内存使用率达到 93.2%，超过阈值 90%', NOW(), 'ACTIVE'),
(3, '磁盘空间不足', 'CRITICAL', 'HOST', 'host-003', 'db-server-03', 'system.disk.usage', 90, 95.7, '主机 db-server-03 磁盘空间使用率达到 95.7%，超过阈值 90%', NOW(), 'ACTIVE'); 