# SKYI监控系统架构设计与数据流转

## 系统架构图

```
+------------------------+     +------------------------+     +------------------------+
|                        |     |                        |     |                        |
|    资产服务            |     |    采集服务            |     |    数据处理服务       |
|  (Asset Service)       |<--->|  (Collector Service)   |---->|  (Processor Service)  |
|                        |     |                        |     |                        |
+------------------------+     +------------------------+     +------------------------+
           ^                               ^                             |
           |                               |                             |
           |                               |                             v
+------------------------+     +------------------------+     +------------------------+
|                        |     |                        |     |                        |
|    可视化服务          |<--->|    存储服务            |<----|    告警服务           |
| (Visualization Service)|     |  (Storage Service)     |<--->|   (Alert Service)     |
|                        |     |                        |     |                        |
+------------------------+     +------------------------+     +------------------------+
           ^                                                          |
           |                                                          |
           +----------------------------------------------------------+
                                       数据流
                                       事件通知
                                       服务调用
```

## 服务间数据流转方式

### 1. 同步通信方式
- **REST API**: 服务间直接HTTP调用
- **gRPC**: 高性能RPC框架，适用于服务间频繁通信
- **GraphQL**: 灵活的数据查询和操作语言

### 2. 异步通信方式
- **消息队列**: Kafka/RabbitMQ用于解耦和流处理
- **事件总线**: 发布-订阅模式传递事件
- **流处理**: 实时数据流的处理和转发

### 3. 数据存储与共享
- **分布式缓存**: Redis用于共享临时数据
- **时序数据库**: InfluxDB存储监控指标数据
- **关系型数据库**: MySQL存储元数据和配置

## 各服务间具体数据流转

### 资产服务 → 采集服务
**流转内容**: 资产基础信息、连接参数、资产变更事件
**通信方式**: 
- REST API: 采集服务调用资产服务API获取资产信息
- 消息队列: 资产变更事件通过Kafka发布，采集服务订阅处理

```json
// 资产信息示例(REST API响应)
{
  "id": 1001,
  "code": "SRV001",
  "name": "应用服务器",
  "type": "server",
  "ipAddress": "192.168.1.100",
  "status": 1,
  "connectionParams": {
    "snmp": {
      "port": 161,
      "community": "public",
      "version": 2
    }
  }
}

// 资产变更事件示例(Kafka消息)
{
  "eventType": "ASSET_UPDATED",
  "assetId": 1001,
  "timestamp": "2023-05-20T08:30:00Z",
  "changes": ["ipAddress", "status"]
}
```

### 采集服务 → 数据处理服务
**流转内容**: 原始采集数据、采集状态事件
**通信方式**: 
- Kafka消息队列: 高吞吐量数据传输
- 数据批处理: 大批量数据一次性传输

```json
// 采集数据消息(Kafka)
{
  "taskId": 10001,
  "assetId": 1001,
  "timestamp": "2023-05-20T08:30:15Z",
  "metrics": [
    {"name": "cpu_usage", "value": 85.2, "unit": "percent"},
    {"name": "memory_usage", "value": 6.8, "unit": "GB"}
  ]
}

// 采集状态事件
{
  "eventType": "COLLECTION_COMPLETED",
  "taskId": 10001,
  "assetId": 1001,
  "timestamp": "2023-05-20T08:30:20Z",
  "status": "SUCCESS",
  "dataPointCount": 25
}
```

### 数据处理服务 → 存储服务
**流转内容**: 处理后的指标数据、聚合数据
**通信方式**: 
- 批量写入API: InfluxDB写入接口
- 流处理管道: 持续数据流处理和存储

```json
// 处理后的批量指标数据
[
  {
    "measurement": "system_metrics",
    "tags": {
      "asset_id": "1001",
      "asset_type": "server",
      "datacenter": "bj-01"
    },
    "time": "2023-05-20T08:30:15Z",
    "fields": {
      "cpu_usage_avg": 82.3,
      "memory_usage_percent": 74.5,
      "disk_io_wait": 2.1
    }
  }
]
```

### 数据处理服务 → 告警服务
**流转内容**: 异常数据、阈值超限事件
**通信方式**: 
- 事件驱动: 基于异常触发的事件通知
- 消息队列: Kafka告警主题

```json
// 阈值超限事件
{
  "eventType": "THRESHOLD_EXCEEDED",
  "assetId": 1001,
  "metricName": "cpu_usage",
  "value": 92.5,
  "threshold": 90.0,
  "timestamp": "2023-05-20T08:32:10Z",
  "duration": 180,  // 持续秒数
  "severity": "WARNING"
}
```

### 存储服务 → 告警服务
**流转内容**: 历史数据查询、趋势分析数据
**通信方式**: 
- 查询API: InfluxDB查询接口
- 订阅模式: 持续查询结果推送

```sql
-- 告警服务查询存储服务的示例(InfluxDB查询)
SELECT mean("value") FROM "cpu_usage" 
WHERE "asset_id" = '1001' AND 
time >= now() - 30m 
GROUP BY time(5m)
```

### 告警服务 → 可视化服务
**流转内容**: 告警状态、告警历史
**通信方式**: 
- REST API: 告警服务提供告警查询API
- WebSocket: 实时告警推送
- 事件总线: 全局告警事件分发

```json
// 告警通知(WebSocket)
{
  "alertId": "alert-2023052008-1001",
  "assetId": 1001,
  "assetName": "应用服务器",
  "metricName": "cpu_usage",
  "severity": "WARNING",
  "message": "CPU使用率超过90%持续3分钟",
  "timestamp": "2023-05-20T08:32:10Z",
  "status": "ACTIVE"
}
```

### 存储服务 → 可视化服务
**流转内容**: 指标数据、历史趋势数据
**通信方式**: 
- REST API: 数据查询接口
- GraphQL: 灵活的数据组合查询
- WebSocket: 实时数据推送

```graphql
# 可视化服务查询存储服务的示例(GraphQL)
query {
  metrics(
    assetId: "1001",
    names: ["cpu_usage", "memory_usage"],
    timeRange: {start: "2023-05-20T08:00:00Z", end: "2023-05-20T09:00:00Z"},
    interval: "5m"
  ) {
    timestamp
    name
    value
    unit
  }
}
```

### 资产服务 → 可视化服务
**流转内容**: 资产拓扑、资产元数据
**通信方式**: 
- REST API: 资产数据查询
- 缓存同步: Redis缓存常用资产数据

```json
// 资产拓扑请求(REST API)
GET /api/assets/topology?type=network&depth=2

// 响应
{
  "nodes": [
    {"id": "1001", "name": "核心交换机", "type": "network"},
    {"id": "1002", "name": "边缘交换机A", "type": "network"},
    {"id": "1003", "name": "边缘交换机B", "type": "network"}
  ],
  "links": [
    {"source": "1001", "target": "1002", "type": "physical"},
    {"source": "1001", "target": "1003", "type": "physical"}
  ]
}
```

## 技术选型建议

### 同步通信
- **Spring Cloud OpenFeign**: 服务间REST调用
- **Spring Cloud Gateway**: API网关
- **Nacos**: 服务注册与发现

### 异步通信
- **Kafka**: 高吞吐量消息队列，适用于指标数据传输
- **Spring Cloud Stream**: 消息驱动的微服务框架
- **Spring Integration**: 企业集成模式实现

### 数据存储
- **InfluxDB**: 时序数据存储
- **MySQL**: 元数据和配置存储
- **Redis**: 缓存和临时数据存储

### 数据序列化
- **JSON**: 通用数据交换格式
- **Protocol Buffers**: 高效二进制序列化(用于大量数据)
- **Avro**: 支持模式演化的序列化格式

## 数据流监控与治理

### 流量监控
- **Prometheus + Grafana**: 监控服务间调用量和延迟
- **Micrometer**: 应用级别指标收集

### 链路追踪
- **Spring Cloud Sleuth + Zipkin**: 分布式追踪
- **OpenTracing**: 标准追踪API

### 熔断与限流
- **Resilience4j**: 限流、熔断、重试
- **Sentinel**: 面向分布式服务架构的流量控制组件

## 部署架构
```
Kubernetes集群
├── 命名空间: skyi-monitor
│   ├── 部署: asset-service (3个副本)
│   ├── 部署: collector-service (5个副本)
│   ├── 部署: processor-service (3个副本)
│   ├── 部署: storage-service (3个副本)
│   ├── 部署: alert-service (2个副本)
│   └── 部署: visualization-service (2个副本)
└── 命名空间: skyi-infrastructure
    ├── 有状态集: kafka (3个节点)
    ├── 有状态集: mysql (主从复制)
    ├── 有状态集: influxdb (集群)
    ├── 部署: redis (主从+哨兵)
    └── 部署: nacos (集群)
```

## 扩展性考虑

1. **服务网格**:
   - 考虑引入Istio等服务网格，统一处理服务间通信
   - 提供流量管理、安全策略和可观测性

2. **事件驱动架构**:
   - 向纯事件驱动架构演进，降低服务间耦合
   - 采用CQRS模式分离读写操作

3. **多租户支持**:
   - 数据隔离设计，支持多租户场景
   - 服务实例池化与租户映射

4. **边缘计算支持**:
   - 采集服务向边缘节点扩展
   - 数据预处理下沉到边缘节点 


   storage-service/
├── src/main/java/com/skyi/storage/
│   ├── config/                    # 配置类
│   │   ├── InfluxDBConfig.java    # InfluxDB配置
│   │   ├── RedisConfig.java       # Redis配置
│   │   └── AsyncConfig.java       # 异步任务配置
│   ├── controller/                # 控制器
│   │   ├── MetricStorageController.java
│   │   └── MetadataController.java
│   ├── service/                   # 服务接口及实现
│   │   ├── timeseries/            # 时序数据服务
│   │   │   ├── TimeSeriesService.java
│   │   │   └── impl/InfluxDBTimeSeriesServiceImpl.java
│   │   ├── metadata/              # 元数据服务
│   │   │   ├── MetadataService.java
│   │   │   └── impl/MySQLMetadataServiceImpl.java
│   │   └── cache/                 # 缓存服务
│   │       ├── CacheService.java
│   │       └── impl/RedisCacheServiceImpl.java
│   ├── model/                     # 数据模型
│   │   ├── MetricData.java        # 指标数据模型
│   │   └── MetadataEntity.java    # 元数据模型
│   ├── repository/                # 数据访问层
│   │   └── MetadataRepository.java
│   ├── dto/                       # 数据传输对象
│   ├── exception/                 # 异常处理
│   └── StorageServiceApplication.java
└── src/main/resources/
    └── application.yml