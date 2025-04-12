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

## 项目结构

```
skyi-monitor/
├── docs/                              # 项目文档
├── docker/                            # Docker配置文件
├── kubernetes/                        # K8s部署配置
├── infrastructure/                    # 基础设施代码
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

## 贡献指南

欢迎提交问题和改进建议。

## 版本信息

- 当前版本：1.0.0-SNAPSHOT
- 发布日期：尚未发布

## 许可证

[Apache License 2.0](LICENSE) 