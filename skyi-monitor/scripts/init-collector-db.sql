-- 使用采集服务数据库
USE skyi_collector;

-- 采集任务表
CREATE TABLE IF NOT EXISTS t_collector_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '任务名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '任务编码',
    type VARCHAR(20) NOT NULL COMMENT '任务类型（server-服务器，database-数据库，middleware-中间件，application-应用）',
    protocol VARCHAR(20) NOT NULL COMMENT '协议类型（snmp,ssh,jdbc,http,jmx,prometheus等）',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    `interval` INT NOT NULL COMMENT '采集间隔(秒)',
    metrics TEXT NOT NULL COMMENT '采集指标列表，JSON字符串',
    connection_params TEXT COMMENT '连接参数，JSON字符串',
    status INT NOT NULL DEFAULT 1 COMMENT '状态（1-启用，0-禁用）',
    description VARCHAR(500) COMMENT '描述',
    last_execute_time DATETIME COMMENT '最后执行时间',
    last_execute_status INT COMMENT '最后执行状态（1-成功，0-失败）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_asset_id (asset_id),
    INDEX idx_type_protocol (type, protocol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采集任务表';

-- 采集任务调度表
CREATE TABLE IF NOT EXISTS t_collector_task_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '调度名称',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    schedule_type INT NOT NULL COMMENT '调度类型（1-固定频率，2-Cron表达式，3-一次性）',
    fixed_rate INT COMMENT '固定频率（秒）',
    cron_expression VARCHAR(100) COMMENT 'Cron表达式',
    execute_time DATETIME COMMENT '执行时间（一次性任务）',
    start_time DATETIME COMMENT '有效期开始时间',
    end_time DATETIME COMMENT '有效期结束时间',
    max_retries INT DEFAULT 0 COMMENT '最大重试次数',
    retry_interval INT DEFAULT 0 COMMENT '重试间隔（秒）',
    enabled INT NOT NULL DEFAULT 1 COMMENT '是否启用（1-启用，0-禁用）',
    last_execute_time DATETIME COMMENT '最后执行时间',
    next_execute_time DATETIME COMMENT '下次执行时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_id (task_id),
    INDEX idx_schedule_type_enabled (schedule_type, enabled),
    INDEX idx_next_execute_time (next_execute_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采集任务调度表';

-- 采集任务实例表
CREATE TABLE IF NOT EXISTS t_collector_task_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    schedule_id BIGINT COMMENT '调度ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    status INT NOT NULL COMMENT '状态（0-进行中，1-成功，2-失败）',
    error_message TEXT COMMENT '错误信息',
    data_point_count INT DEFAULT 0 COMMENT '数据点数量',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_id (task_id),
    INDEX idx_asset_id (asset_id),
    INDEX idx_schedule_id (schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采集任务实例表';

-- 采集指标数据表
CREATE TABLE IF NOT EXISTS t_collector_metric_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    instance_id BIGINT NOT NULL COMMENT '任务实例ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    metric_name VARCHAR(100) NOT NULL COMMENT '指标名称',
    metric_labels TEXT COMMENT '指标标签，JSON字符串',
    metric_value DOUBLE NOT NULL COMMENT '指标值',
    collect_time DATETIME NOT NULL COMMENT '采集时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_instance (task_id, instance_id),
    INDEX idx_asset_metric (asset_id, metric_name),
    INDEX idx_collect_time (collect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采集指标数据表';

-- 采集代理表
CREATE TABLE IF NOT EXISTS t_collector_agent (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '代理名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '代理编码',
    ip VARCHAR(50) NOT NULL COMMENT 'IP地址',
    port INT NOT NULL COMMENT '端口',
    version VARCHAR(20) COMMENT '版本',
    status INT NOT NULL DEFAULT 1 COMMENT '状态（1-在线，0-离线）',
    last_heartbeat_time DATETIME COMMENT '最后心跳时间',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_code (code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采集代理表'; 