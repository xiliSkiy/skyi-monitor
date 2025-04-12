# SKYI监控系统前端

## 项目描述

SKYI监控系统的前端项目，基于Vue 3、TypeScript、Vite和Element Plus开发，用于展示和管理企业级监控系统的资产、监控数据和告警等功能。

## 功能特点

- 仪表盘数据可视化
- 资产管理（CRUD操作）
- 监控数据展示
- 告警查看和处理
- 系统配置管理

## 技术栈

- Vue 3
- TypeScript
- Vite
- Element Plus
- ECharts
- Vue Router
- Pinia
- Axios

## 开发环境设置

### 先决条件

- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

### 构建项目

```bash
npm run build
```

## 项目结构

```
frontend/
├── public/                      # 静态资源
├── src/
│   ├── api/                     # API接口
│   ├── assets/                  # 项目资源
│   ├── components/              # 公共组件
│   ├── views/                   # 页面视图
│   ├── router/                  # 路由配置
│   ├── store/                   # 状态管理
│   ├── utils/                   # 工具函数
│   ├── plugins/                 # 插件配置
│   ├── types/                   # 类型定义
│   ├── App.vue                  # 根组件
│   └── main.ts                  # 入口文件
├── .env                         # 环境变量
├── index.html                   # HTML模板
├── tsconfig.json                # TypeScript配置
├── vite.config.ts               # Vite配置
└── package.json                 # 项目依赖
```

## 连接后端

前端默认连接到 `http://localhost:8081` 的后端API。可以通过修改 `.env` 文件中的 `VITE_API_BASE_URL` 环境变量来更改API基础URL。 