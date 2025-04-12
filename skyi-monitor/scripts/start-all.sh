#!/bin/bash

# 一键启动所有服务脚本

echo "========== 一键启动SKYI监控系统 =========="

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 添加脚本执行权限
echo "添加脚本执行权限..."
chmod +x "${SCRIPT_DIR}/restart-dev.sh"
chmod +x "${SCRIPT_DIR}/start-dev.sh"
chmod +x "${SCRIPT_DIR}/start-collector.sh"

# 重启开发环境
echo "重启开发环境..."
bash "${SCRIPT_DIR}/restart-dev.sh"

# 等待基础设施服务完全启动
echo "等待基础设施服务完全启动..."
sleep 20

# 启动前端服务
if [ -d "${PROJECT_ROOT}/frontend" ]; then
  echo "========== 启动前端服务 =========="
  cd "${PROJECT_ROOT}/frontend" || exit 1
  
  # 检查是否安装了依赖
  if [ ! -d "node_modules" ]; then
    echo "安装前端依赖..."
    npm install
  fi
  
  # 在后台启动前端服务
  echo "在后台启动前端服务..."
  nohup npm run dev > "${PROJECT_ROOT}/frontend/frontend.log" 2>&1 &
  echo "前端服务已在后台启动，日志保存在 ${PROJECT_ROOT}/frontend/frontend.log"
  echo "访问地址: http://localhost:3000"
fi

# 启动采集服务
echo "启动采集服务..."
exec "${SCRIPT_DIR}/start-collector.sh"

# 注意: 这里使用exec，所以这之后的代码不会执行
# 如果需要启动多个服务，应该将它们写在start-collector.sh之前，并使用后台运行方式 