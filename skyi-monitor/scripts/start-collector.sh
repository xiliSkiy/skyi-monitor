#!/bin/bash

# 启动采集服务脚本

echo "========== 启动采集服务 =========="

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
COLLECTOR_DIR="${PROJECT_ROOT}/microservices/collector-service"

# 检查环境服务是否已启动
echo "检查环境服务..."
required_services=(
  "skyi-mysql"
  "skyi-redis"
  "skyi-nacos"
  "skyi-kafka"
)

need_restart=false

for service in "${required_services[@]}"; do
  if ! [ "$(docker ps -q -f name=${service})" ]; then
    echo "❌ ${service} 未运行"
    need_restart=true
  else
    echo "✅ ${service} 已运行"
  fi
done

if $need_restart; then
  echo "环境服务未完全启动，正在重启环境..."
  bash "${SCRIPT_DIR}/restart-dev.sh"
else
  echo "环境服务已全部启动，无需重启"
fi

# 检查采集服务目录
if [ ! -d "$COLLECTOR_DIR" ]; then
  echo "错误: 采集服务目录不存在: $COLLECTOR_DIR" >&2
  exit 1
fi

# 切换到采集服务目录
cd "$COLLECTOR_DIR" || exit 1

# 检查Maven Wrapper是否存在
if [ ! -f "./mvnw" ]; then
  echo "Maven Wrapper不存在，尝试创建..."
  if command -v mvn &> /dev/null; then
    mvn -N io.takari:maven:wrapper
    chmod +x ./mvnw
  else
    echo "错误: Maven未安装，无法创建Wrapper" >&2
    exit 1
  fi
fi

# 给Maven Wrapper添加执行权限
chmod +x ./mvnw

# 启动采集服务
echo "正在启动采集服务..."
echo "输出将显示在控制台，按Ctrl+C停止服务"
echo "========== 采集服务日志 =========="

# 使用Maven Wrapper启动采集服务
./mvnw spring-boot:run 