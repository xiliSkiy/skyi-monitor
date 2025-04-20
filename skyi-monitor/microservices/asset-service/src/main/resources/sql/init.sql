-- 创建数据库(如果不存在)
CREATE DATABASE IF NOT EXISTS skyi_asset DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE skyi_asset;

-- 创建资产表
CREATE TABLE IF NOT EXISTS asset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_uuid VARCHAR(50) NOT NULL COMMENT '资产UUID',
    name VARCHAR(100) NOT NULL COMMENT '资产名称',
    asset_type VARCHAR(50) NOT NULL COMMENT '资产类型(SERVER/DATABASE/MIDDLEWARE/APPLICATION)',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    port INT COMMENT '端口号',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/INACTIVE/MAINTAINING/ERROR)',
    description TEXT COMMENT '描述',
    owner VARCHAR(50) COMMENT '负责人',
    location VARCHAR(100) COMMENT '位置',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_asset_uuid (asset_uuid)
) COMMENT='资产表';

-- 创建资产属性表
CREATE TABLE IF NOT EXISTS asset_property (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    property_key VARCHAR(50) NOT NULL COMMENT '属性键',
    property_value VARCHAR(255) COMMENT '属性值',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (asset_id) REFERENCES asset(id) ON DELETE CASCADE,
    UNIQUE KEY uk_asset_property (asset_id, property_key)
) COMMENT='资产属性表';

-- 创建资产关系表
CREATE TABLE IF NOT EXISTS asset_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_asset_id BIGINT NOT NULL COMMENT '源资产ID',
    target_asset_id BIGINT NOT NULL COMMENT '目标资产ID',
    relation_type VARCHAR(50) NOT NULL COMMENT '关系类型(DEPENDS_ON/CONTAINS/CONNECTS_TO)',
    description VARCHAR(255) COMMENT '关系描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (source_asset_id) REFERENCES asset(id) ON DELETE CASCADE,
    FOREIGN KEY (target_asset_id) REFERENCES asset(id) ON DELETE CASCADE,
    UNIQUE KEY uk_asset_relation (source_asset_id, target_asset_id, relation_type)
) COMMENT='资产关系表';

-- 创建资产组表
CREATE TABLE IF NOT EXISTS asset_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '资产组名称',
    parent_id BIGINT COMMENT '父组ID',
    description VARCHAR(255) COMMENT '描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES asset_group(id) ON DELETE SET NULL,
    UNIQUE KEY uk_group_name (name)
) COMMENT='资产组表';

-- 创建资产组成员表
CREATE TABLE IF NOT EXISTS asset_group_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL COMMENT '资产组ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES asset_group(id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES asset(id) ON DELETE CASCADE,
    UNIQUE KEY uk_group_asset (group_id, asset_id)
) COMMENT='资产组成员表';

-- 插入示例数据
INSERT INTO asset (asset_uuid, name, asset_type, ip_address, port, status, description, owner, location) VALUES
('SRV-001', '生产服务器001', 'SERVER', '192.168.1.100', NULL, 'ACTIVE', '主要生产服务器', '张三', '北京机房A区'),
('SRV-002', '测试服务器001', 'SERVER', '192.168.1.101', NULL, 'ACTIVE', '测试环境服务器', '李四', '北京机房B区'),
('DB-001', 'MySQL主库', 'DATABASE', '192.168.1.200', 3306, 'ACTIVE', '生产环境主数据库', '王五', '北京机房A区'),
('APP-001', '订单系统', 'APPLICATION', '192.168.1.150', 8080, 'ACTIVE', '核心订单处理系统', '赵六', '北京机房A区');

INSERT INTO asset_property (asset_id, property_key, property_value) VALUES
(1, 'os', 'CentOS 7.9'),
(1, 'cpu_cores', '16'),
(1, 'memory', '64GB'),
(1, 'disk', '2TB'),
(2, 'os', 'Ubuntu 20.04'),
(2, 'cpu_cores', '8'),
(2, 'memory', '32GB'),
(3, 'version', '8.0.28'),
(3, 'storage', '500GB'),
(4, 'version', '2.0'),
(4, 'framework', 'Spring Boot');

INSERT INTO asset_group (name, description) VALUES
('生产环境', '所有生产环境资产'),
('测试环境', '所有测试环境资产'),
('核心系统', '核心业务系统相关资产');

INSERT INTO asset_group_member (group_id, asset_id) VALUES
(1, 1), (1, 3), (1, 4),
(2, 2),
(3, 3), (3, 4);

INSERT INTO asset_relation (source_asset_id, target_asset_id, relation_type, description) VALUES
(4, 3, 'DEPENDS_ON', '订单系统依赖MySQL主库'),
(4, 1, 'DEPLOYED_ON', '订单系统部署在生产服务器001上');

-- 给予所有用户访问权限
GRANT ALL PRIVILEGES ON skyi_asset.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES; 