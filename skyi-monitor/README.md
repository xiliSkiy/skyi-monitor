# SKYI 企业级监控系统

## 项目概述

SKYI 监控系统是一个全面的企业级监控解决方案，用于监控服务器、中间件和应用程序的性能和健康状况。系统采用微服务架构，支持高可用、高性能、可扩展的部署方式。

## 技术栈

- **后端**：Spring Boot, Spring Cloud, JPA, MySQL, InfluxDB, Redis, Kafka, Nacos
- **前端**：Vue.js 3, Element Plus, ECharts
- **部署**：Docker, Kubernetes

## 功能特点

- 多维度资产管理
- 多协议数据采集
- 实时数据处理和分析
- 灵活的告警策略
- 丰富的可视化展示
- 完善的权限控制

## 系统架构

系统采用分层架构设计，包括：
- 数据采集层
- 数据处理层
- 数据存储层
- 应用服务层
- 展示层

### 消息处理流程

系统使用Kafka作为消息队列，实现数据采集和处理的解耦：

1. 采集服务从目标资产收集指标数据
2. 采集的数据被发送到Kafka的`collector-metric-data`主题
3. 处理服务从Kafka消费数据并进行处理
4. 处理后的数据被存储到InfluxDB
5. 告警服务监控数据并触发相应的告警

## 快速开始

### 前置条件

- JDK 11+
- Maven 3.6+
- Docker & Docker Compose
- MySQL 8.0
- Redis 6.2
- InfluxDB 2.6
- Nacos 2.2.0
- Kafka 3.0+

### 开发环境启动

1. 克隆代码仓库
    ```
    git clone https://github.com/your-username/skyi-monitor.git
    cd skyi-monitor
    ```

2. 启动基础设施服务
    ```
    # 使用Docker Compose启动
    cd docker
    docker-compose up -d
    
    # 或者使用脚本启动
    ./scripts/start-dev.sh
    
    # 单独启动Kafka
    cd infrastructure/kafka
    ./start-kafka.sh
    ```

3. 编译项目
    ```
    mvn clean package -DskipTests
    ```

4. 启动资产管理服务
    ```
    java -jar microservices/asset-service/target/asset-service-1.0.0-SNAPSHOT.jar
    ```

5. 访问服务
    - 资产管理服务: http://localhost:8081
    - Nacos控制台: http://localhost:8848/nacos
    - InfluxDB控制台: http://localhost:8086
    - Kafka控制台: http://localhost:8080

## 项目结构

```
skyi-monitor/
├── docs/                              # 项目文档
├── docker/                            # Docker配置文件
├── kubernetes/                        # K8s部署配置
├── infrastructure/                    # 基础设施代码
│   ├── kafka/                         # Kafka配置和工具
│   │   ├── config/                    # Kafka配置文件
│   │   ├── docker/                    # Kafka Docker配置
│   │   └── utils/                     # Kafka测试工具
├── microservices/                     # 微服务模块
│   ├── asset-service/                 # 资产管理服务
│   ├── collector-service/             # 数据采集服务
│   ├── processor-service/             # 数据处理服务
│   ├── storage-service/               # 存储服务
│   ├── alert-service/                 # 告警服务
│   ├── visualization-service/         # 可视化服务
│   └── auth-service/                  # 安全与权限服务
├── frontend/                          # 前端项目
├── common/                            # 公共组件
├── agent/                             # 监控代理程序
└── scripts/                           # 构建部署脚本
```

## 开发指南

详细的开发指南请参阅 [开发文档](docs/README.md)。

### Kafka 使用指南

Kafka 作为系统的消息队列组件，用于实现采集服务和处理服务之间的数据传输，详细使用方法：

1. **启动 Kafka**
   ```
   cd infrastructure/kafka
   ./start-kafka.sh
   ```

2. **创建测试主题**
   ```
   ./create-topics.sh
   ```

3. **测试消息发送和接收**
   ```
   # 打包工具
   ./package-utils.sh
   
   # 使用打包好的工具
   cd target
   unzip kafka-utils-1.0.0.zip
   cd kafka-utils-1.0.0
   
   # 运行消费者
   ./run-consumer.sh
   
   # 新开一个终端，运行生产者
   ./run-producer.sh
   ```

4. **停止 Kafka**
   ```
   cd infrastructure/kafka
   ./stop-kafka.sh
   ```

详细的 Kafka 配置和使用指南请参阅 [Kafka 文档](infrastructure/kafka/README.md)。

## 贡献指南

欢迎提交问题和改进建议。

## 版本信息

- 当前版本：1.0.0-SNAPSHOT
- 发布日期：尚未发布

## 许可证

[Apache License 2.0](LICENSE) 