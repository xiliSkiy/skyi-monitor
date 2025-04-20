-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `skyi_visualization` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `skyi_visualization`;

-- 创建仪表盘表
CREATE TABLE IF NOT EXISTS `dashboard` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '仪表盘名称',
  `description` varchar(500) DEFAULT NULL COMMENT '仪表盘描述',
  `layout` text NOT NULL COMMENT '布局配置(JSON)',
  `creator` varchar(50) NOT NULL COMMENT '创建者',
  `is_public` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否公开',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_creator` (`creator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仪表盘表';

-- 创建仪表盘组件表
CREATE TABLE IF NOT EXISTS `dashboard_component` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dashboard_id` bigint(20) NOT NULL COMMENT '仪表盘ID',
  `name` varchar(100) NOT NULL COMMENT '组件名称',
  `component_type` varchar(50) NOT NULL COMMENT '组件类型',
  `position_x` int(11) NOT NULL COMMENT 'X坐标',
  `position_y` int(11) NOT NULL COMMENT 'Y坐标',
  `width` int(11) NOT NULL COMMENT '宽度',
  `height` int(11) NOT NULL COMMENT '高度',
  `config` text NOT NULL COMMENT '组件配置(JSON)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_dashboard_id` (`dashboard_id`),
  CONSTRAINT `fk_component_dashboard` FOREIGN KEY (`dashboard_id`) REFERENCES `dashboard` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仪表盘组件表';

-- 创建报表表
CREATE TABLE IF NOT EXISTS `report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '报表名称',
  `description` varchar(500) DEFAULT NULL COMMENT '报表描述',
  `template` text NOT NULL COMMENT '报表模板(JSON)',
  `schedule` varchar(50) DEFAULT NULL COMMENT '报表计划(CRON表达式)',
  `recipients` varchar(500) DEFAULT NULL COMMENT '接收者(邮箱，逗号分隔)',
  `last_generated` datetime DEFAULT NULL COMMENT '最后生成时间',
  `creator` varchar(50) NOT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_creator` (`creator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表表';

-- 插入示例数据
INSERT INTO `dashboard` (`name`, `description`, `layout`, `creator`, `is_public`) VALUES
('系统监控仪表盘', '显示系统的关键性能指标', '{"columns": 12, "rowHeight": 30, "compactType": "vertical", "preventCollision": false}', 'admin', 1),
('服务健康状态', '服务的健康状态监控', '{"columns": 12, "rowHeight": 30, "compactType": "vertical", "preventCollision": false}', 'admin', 1);

INSERT INTO `dashboard_component` (`dashboard_id`, `name`, `component_type`, `position_x`, `position_y`, `width`, `height`, `config`) VALUES
(1, 'CPU使用率', 'line-chart', 0, 0, 6, 4, '{"metric": "system.cpu.usage", "aggregator": "avg", "interval": "1m", "timeRange": "1h"}'),
(1, '内存使用率', 'line-chart', 6, 0, 6, 4, '{"metric": "system.memory.usage", "aggregator": "avg", "interval": "1m", "timeRange": "1h"}'),
(1, '磁盘I/O', 'line-chart', 0, 4, 6, 4, '{"metric": "system.disk.io", "aggregator": "sum", "interval": "1m", "timeRange": "1h"}'),
(1, '网络流量', 'line-chart', 6, 4, 6, 4, '{"metric": "system.network.traffic", "aggregator": "sum", "interval": "1m", "timeRange": "1h"}'),
(2, '服务状态', 'status-card', 0, 0, 12, 3, '{"services": ["asset-service", "collector-service", "processor-service", "storage-service", "visualization-service"]}'),
(2, '端点响应时间', 'bar-chart', 0, 3, 12, 4, '{"metric": "http.server.requests", "aggregator": "avg", "interval": "1m", "timeRange": "1h", "groupBy": "uri"}');

INSERT INTO `report` (`name`, `description`, `template`, `schedule`, `recipients`, `creator`) VALUES
('每日系统性能报告', '系统性能的每日摘要', '{"metrics": ["system.cpu.usage", "system.memory.usage", "system.disk.io", "system.network.traffic"], "timeRange": "1d", "aggregator": "avg", "interval": "1h"}', '0 0 0 * * ?', 'admin@example.com', 'admin'),
('每周服务健康报告', '服务健康状态的每周摘要', '{"metrics": ["http.server.requests", "http.client.requests"], "timeRange": "7d", "aggregator": "avg", "interval": "1d"}', '0 0 0 ? * MON', 'admin@example.com,ops@example.com', 'admin'); 