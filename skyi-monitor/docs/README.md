# SKYI 企业级监控系统

## 项目概述

SKYI 监控系统是一个全面的企业级监控解决方案，用于监控服务器、中间件和应用程序的性能和健康状况。系统采用微服务架构，支持高可用、高性能、可扩展的部署方式。

## 系统架构

系统采用分层架构设计，包括：
- 数据采集层
- 数据处理层
- 数据存储层
- 应用服务层
- 展示层

## 核心组件

### 1. 资产管理服务
- 设备自动发现与注册
- 资产分类与标签管理
- 资产拓扑管理

### 2. 数据采集服务
- 支持多种协议采集
- 采集任务调度
- Agent管理

### 3. 数据处理服务
- 数据过滤和转换
- 数据聚合计算
- 告警规则引擎

### 4. 存储服务
- 时序数据存储
- 元数据管理
- 缓存服务

### 5. 告警服务
- 告警生成与路由
- 多渠道通知
- 告警处理跟踪

### 6. 可视化服务
- 实时监控大屏
- 自定义仪表盘
- 报表生成

### 7. 安全与权限服务
- 用户认证
- 权限管理
- 审计日志

## 技术栈

### 后端
- 编程语言：Java
- 微服务框架：Spring Boot, Spring Cloud
- 数据库：InfluxDB, MySQL, Redis
- 消息队列：Kafka
- 服务注册与发现：Nacos
- 任务调度：XXL-Job

### 前端
- 框架：Vue.js 3
- UI组件：Element Plus
- 图表库：ECharts
- 状态管理：Pinia

## 部署方式

系统支持基于Kubernetes的容器化部署，可实现高可用无单点故障的生产环境。

## 项目结构

```
skyi-monitor/
├── docs/                             # 项目文档
├── docker/                           # Docker配置文件
├── kubernetes/                       # K8s部署配置
├── infrastructure/                   # 基础设施代码
├── microservices/                    # 微服务模块
│   ├── asset-service/                # 资产管理服务
│   ├── collector-service/            # 数据采集服务
│   ├── processor-service/            # 数据处理服务
│   ├── storage-service/              # 存储服务
│   ├── alert-service/                # 告警服务
│   ├── visualization-service/        # 可视化服务
│   └── auth-service/                 # 安全与权限服务
├── frontend/                         # 前端项目
├── common/                           # 公共组件
├── agent/                            # 监控代理程序
└── scripts/                          # 构建部署脚本
```

## 快速开始

详细的安装和使用说明见各服务目录下的README文件。

## 贡献指南

欢迎提交问题和改进建议。

## 版本信息

- 当前版本：1.0.0-SNAPSHOT
- 发布日期：尚未发布

## 许可证

[Apache License 2.0](LICENSE) 