#!/bin/bash

# 重启开发环境脚本

echo "========== 重启开发环境 =========="

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 停止并删除已存在的容器
echo "停止并删除现有容器..."
containers=(
  "skyi-mysql"
  "skyi-redis" 
  "skyi-influxdb" 
  "skyi-nacos" 
  "skyi-zookeeper" 
  "skyi-kafka"
  "skyi-xxl-job-admin"
)

for container in "${containers[@]}"; do
  if [ "$(docker ps -q -f name=${container})" ]; then
    echo "停止并删除容器: ${container}"
    docker stop ${container} >/dev/null 2>&1
    docker rm ${container} >/dev/null 2>&1
  elif [ "$(docker ps -a -q -f name=${container})" ]; then
    echo "删除停止的容器: ${container}"
    docker rm ${container} >/dev/null 2>&1
  fi
done

# 清理无用卷（可选）
# docker volume prune -f

# 执行启动脚本
echo "执行启动脚本..."
bash "${SCRIPT_DIR}/start-dev.sh"

echo "等待服务启动完成..."
sleep 5

# 检查服务状态
echo "检查服务状态..."
for container in "${containers[@]}"; do
  if [ "$(docker ps -q -f name=${container})" ]; then
    echo "✅ ${container} 已成功启动"
  else
    echo "❌ ${container} 启动失败或未运行"
  fi
done

echo "========== 环境重启完成 =========="
echo "你现在可以启动应用服务了"
echo "执行命令：cd ${PROJECT_ROOT}/microservices/collector-service && ./mvnw spring-boot:run" 