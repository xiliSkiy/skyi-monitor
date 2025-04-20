# Kafka 消息队列服务

本目录包含SKYI监控系统使用的Kafka消息队列服务的配置和工具。

## 目录结构

```
kafka/
├── config/                # Kafka配置文件
│   ├── server.properties  # Kafka服务器配置
│   └── zookeeper.properties # Zookeeper配置
├── data/                  # 数据目录（运行时创建）
│   ├── kafka/            # Kafka数据
│   └── zookeeper/        # Zookeeper数据
├── docker/                # Docker相关文件
│   └── docker-compose.yml # Docker Compose配置
├── utils/                 # 工具类
│   ├── KafkaProducerTester.java # 生产者测试工具
│   └── KafkaConsumerTester.java # 消费者测试工具
├── start-kafka.sh         # 启动脚本
├── stop-kafka.sh          # 停止脚本
└── README.md              # 说明文档
```

## 快速开始

### 启动Kafka服务

执行以下命令启动Kafka和Zookeeper服务：

```bash
./start-kafka.sh
```

服务启动后，可以通过以下地址访问：

- Kafka: localhost:9092
- Zookeeper: localhost:2181
- Kafka UI管理界面: http://localhost:8080

### 停止Kafka服务

执行以下命令停止服务：

```bash
./stop-kafka.sh
```

## 常用主题

系统使用的主要Kafka主题：

1. `collector-metric-data` - 采集的指标数据
2. `collector-status` - 采集任务状态

## 测试工具使用

### 生产者测试工具

用于测试向Kafka发送消息：

```bash
# 编译
javac -cp "/path/to/dependencies/*" utils/KafkaProducerTester.java

# 运行
java -cp ".:/path/to/dependencies/*" com.skyi.infrastructure.kafka.utils.KafkaProducerTester
```

### 消费者测试工具

用于测试从Kafka接收消息：

```bash
# 编译
javac -cp "/path/to/dependencies/*" utils/KafkaConsumerTester.java

# 运行
java -cp ".:/path/to/dependencies/*" com.skyi.infrastructure.kafka.utils.KafkaConsumerTester
```

## 消息格式

### 指标数据格式

```json
{
  "metricName": "cpu.usage",
  "value": 75.5,
  "timestamp": "2023-04-15T10:15:30.00Z",
  "assetId": 1001,
  "taskId": 2001,
  "instanceId": 3001,
  "tags": {
    "host": "server-1",
    "core": "core-0",
    "env": "prod"
  },
  "fields": {
    "processCount": 125,
    "threadCount": 1250,
    "memoryUsage": 42.5
  }
}
```

### 采集状态格式

```json
{
  "taskId": 2001,
  "instanceId": 3001,
  "assetId": 1001,
  "success": true,
  "startTime": 1649951730000,
  "endTime": 1649951735000,
  "successCount": 10,
  "failCount": 0,
  "errorMessage": null,
  "timestamp": 1649951735500
}
```

## 配置说明

### Kafka配置

主要配置项位于`config/server.properties`：

- `broker.id` - 服务器ID
- `listeners` - 监听地址
- `zookeeper.connect` - Zookeeper连接地址
- `log.retention.hours` - 日志保留时间

### Zookeeper配置

主要配置项位于`config/zookeeper.properties`：

- `dataDir` - 数据目录
- `clientPort` - 客户端端口
- `tickTime` - 会话超时时间

## 故障排查

1. 服务无法启动
   - 检查端口是否被占用：`netstat -an | grep 9092`
   - 检查日志：`docker logs skyi-kafka`

2. 无法连接到Kafka
   - 确保网络连接：`telnet localhost 9092`
   - 检查防火墙设置

3. 消息发送/接收失败
   - 检查主题是否存在：`kafka-topics.sh --list --bootstrap-server localhost:9092`
   - 查看消费者组状态：`kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group processor-group` 