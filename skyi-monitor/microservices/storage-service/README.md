# 存储服务 (Storage Service)

## 概述

存储服务是企业级监控系统中负责数据存储的核心服务，提供时间序列数据、元数据和缓存数据的存储和管理功能。

## 技术栈

- **Spring Boot**: Web框架
- **Spring Data JPA**: 关系型数据库ORM
- **InfluxDB**: 时序数据库
- **Redis**: 缓存数据库
- **MySQL**: 元数据存储
- **Nacos**: 服务注册与发现、配置中心

## 主要功能

### 1. 时间序列数据存储

- 支持高性能的时序数据写入和查询
- 提供数据聚合和降采样功能
- 支持根据多种条件查询时序数据
- 支持数据分组和统计

### 2. 元数据管理

- 提供对元数据的CRUD操作
- 支持按类型、关联ID和元数据键进行查询
- 支持模糊查询和批量操作

### 3. 缓存服务

- 提供通用的缓存操作接口
- 支持String、Hash、List、Set等数据结构
- 支持过期时间设置和管理

## API接口

### 时间序列数据接口

| 接口路径 | 方法 | 说明 |
| --- | --- | --- |
| `/api/v1/timeseries/data` | POST | 保存单条时间序列数据 |
| `/api/v1/timeseries/batch` | POST | 批量保存时间序列数据 |
| `/api/v1/timeseries/query` | POST | 查询时间序列数据 |
| `/api/v1/timeseries/latest` | GET | 查询最新数据 |
| `/api/v1/timeseries/group` | POST | 分组统计数据 |
| `/api/v1/timeseries/data` | DELETE | 删除时间序列数据 |

### 元数据接口

| 接口路径 | 方法 | 说明 |
| --- | --- | --- |
| `/api/v1/metadata` | POST | 保存元数据 |
| `/api/v1/metadata/batch` | POST | 批量保存元数据 |
| `/api/v1/metadata/{id}` | GET | 根据ID查询元数据 |
| `/api/v1/metadata/type/{type}/ref/{refId}` | GET | 根据类型和关联ID查询元数据列表 |
| `/api/v1/metadata/type/{type}/ref/{refId}/key/{metaKey}` | GET | 根据类型、关联ID和元数据键查询元数据 |
| `/api/v1/metadata/search/key` | GET | 根据元数据键模糊查询 |
| `/api/v1/metadata/search/value` | GET | 根据元数据值模糊查询 |
| `/api/v1/metadata` | PUT | 更新元数据 |
| `/api/v1/metadata/{id}` | DELETE | 根据ID删除元数据 |
| `/api/v1/metadata/type/{type}/ref/{refId}` | DELETE | 根据类型和关联ID删除元数据 |
| `/api/v1/metadata/type/{type}/ref/{refId}/key/{metaKey}` | DELETE | 根据类型、关联ID和元数据键删除元数据 |

### 缓存接口

| 接口路径 | 方法 | 说明 |
| --- | --- | --- |
| `/api/v1/cache` | POST | 设置缓存 |
| `/api/v1/cache/expire` | POST | 设置缓存并设置过期时间 |
| `/api/v1/cache` | GET | 获取缓存 |
| `/api/v1/cache` | DELETE | 删除缓存 |
| `/api/v1/cache/pattern` | DELETE | 批量删除缓存 |
| `/api/v1/cache/expire` | PUT | 设置缓存过期时间 |
| `/api/v1/cache/exist` | GET | 判断缓存是否存在 |
| `/api/v1/cache/expire` | GET | 获取缓存过期时间 |

## 配置说明

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:skyi_storage}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: ${REDIS_DATABASE:0}

# InfluxDB配置
influxdb:
  url: ${INFLUXDB_URL:http://localhost:8086}
  token: ${INFLUXDB_TOKEN:your-token}
  org: ${INFLUXDB_ORG:skyi}
  bucket: ${INFLUXDB_BUCKET:monitoring}
```

## 部署说明

### 环境要求

- JDK 11+
- MySQL 8.0+
- InfluxDB 2.0+
- Redis 6.0+
- Nacos 2.0+

### 启动命令

```bash
# 开发环境
java -jar storage-service.jar --spring.profiles.active=dev

# 生产环境
java -jar storage-service.jar --spring.profiles.active=prod
```

## 使用示例

### 保存时间序列数据

```http
POST /api/v1/timeseries/data
Content-Type: application/json

{
  "assetId": "server-001",
  "metricName": "cpu_usage",
  "value": 45.5,
  "unit": "%",
  "tags": {
    "host": "host1",
    "region": "cn-north"
  }
}
```

### 查询最新数据

```http
GET /api/v1/timeseries/latest?assetId=server-001&metricName=cpu_usage
```

### 保存元数据

```http
POST /api/v1/metadata
Content-Type: application/json

{
  "type": "server",
  "refId": "server-001",
  "metaKey": "location",
  "metaValue": "机房A-3层-R12机柜"
}
```

### 设置缓存

```http
POST /api/v1/cache?key=server:status:server-001&value=running
```

## 常见问题

1. **问题**: 时序数据写入失败
   **解决方案**: 检查InfluxDB连接配置是否正确，确保token有写权限

2. **问题**: 查询数据返回为空
   **解决方案**: 检查查询条件是否正确，特别是时间范围和标签过滤条件

3. **问题**: 缓存操作失败
   **解决方案**: 检查Redis连接是否正常，可通过Redis客户端工具进行连接测试 