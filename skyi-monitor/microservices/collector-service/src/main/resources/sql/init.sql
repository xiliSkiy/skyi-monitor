-- 创建数据库(如果不存在)
CREATE DATABASE IF NOT EXISTS skyi_collector DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE skyi_collector;

-- 创建采集目标表
CREATE TABLE IF NOT EXISTS collection_target (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '采集目标名称',
    type VARCHAR(50) NOT NULL COMMENT '目标类型(SERVER/DATABASE/MIDDLEWARE/APPLICATION)',
    host VARCHAR(255) NOT NULL COMMENT '主机地址',
    port INT COMMENT '端口号',
    protocol VARCHAR(50) NOT NULL COMMENT '采集协议(SNMP/SSH/JDBC/HTTP/JMX/PROMETHEUS)',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/INACTIVE/ERROR)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name (name)
) COMMENT='采集目标信息表';

-- 创建采集指标表
CREATE TABLE IF NOT EXISTS collection_metric (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_id BIGINT NOT NULL COMMENT '采集目标ID',
    metric_name VARCHAR(100) NOT NULL COMMENT '指标名称',
    metric_path VARCHAR(255) NOT NULL COMMENT '指标路径',
    collection_interval INT DEFAULT 60 COMMENT '采集间隔(秒)',
    metric_type VARCHAR(20) DEFAULT 'GAUGE' COMMENT '指标类型(GAUGE/COUNTER/HISTOGRAM)',
    description VARCHAR(255) COMMENT '指标描述',
    unit VARCHAR(30) COMMENT '单位',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/INACTIVE)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (target_id) REFERENCES collection_target(id) ON DELETE CASCADE,
    UNIQUE KEY uk_target_metric (target_id, metric_name)
) COMMENT='采集指标配置表';

-- 创建采集任务表
CREATE TABLE IF NOT EXISTS collection_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_id BIGINT NOT NULL COMMENT '采集目标ID',
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型',
    execution_time TIMESTAMP COMMENT '执行时间',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态(PENDING/RUNNING/COMPLETED/FAILED)',
    execution_result TEXT COMMENT '执行结果',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (target_id) REFERENCES collection_target(id) ON DELETE CASCADE
) COMMENT='采集任务表';

-- 创建采集凭据表(存储加密的凭据信息)
CREATE TABLE IF NOT EXISTS collection_credential (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_id BIGINT NOT NULL COMMENT '采集目标ID',
    credential_type VARCHAR(50) NOT NULL COMMENT '凭据类型(USERNAME_PASSWORD/SSH_KEY/API_KEY/TOKEN)',
    username VARCHAR(100) COMMENT '用户名',
    password VARCHAR(255) COMMENT '密码(加密存储)',
    private_key TEXT COMMENT '私钥(加密存储)',
    token VARCHAR(255) COMMENT 'Token(加密存储)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (target_id) REFERENCES collection_target(id) ON DELETE CASCADE,
    UNIQUE KEY uk_target_credential (target_id, credential_type)
) COMMENT='采集凭据表';

-- 插入示例数据
INSERT INTO collection_target (name, type, host, port, protocol) VALUES
('localhost-server', 'SERVER', 'localhost', NULL, 'SNMP'),
('mysql-database', 'DATABASE', 'localhost', 3306, 'JDBC'),
('spring-app', 'APPLICATION', 'localhost', 8080, 'HTTP');

INSERT INTO collection_metric (target_id, metric_name, metric_path, collection_interval, metric_type, description, unit) VALUES
(1, 'cpu_usage', '/system/cpu/usage', 30, 'GAUGE', 'CPU使用率', '%'),
(1, 'memory_usage', '/system/memory/usage', 30, 'GAUGE', '内存使用率', '%'),
(1, 'disk_usage', '/system/disk/usage', 60, 'GAUGE', '磁盘使用率', '%'),
(2, 'connections', '/database/connections', 60, 'GAUGE', '数据库连接数', '个'),
(2, 'query_time', '/database/performance/query_time', 60, 'GAUGE', '查询响应时间', 'ms'),
(3, 'response_time', '/application/response_time', 30, 'GAUGE', '应用响应时间', 'ms'),
(3, 'error_rate', '/application/error_rate', 30, 'GAUGE', '错误率', '%');

-- 给予所有用户访问权限
GRANT ALL PRIVILEGES ON skyi_collector.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES; 