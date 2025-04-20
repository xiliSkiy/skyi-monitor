#!/bin/bash

# SKYI监控系统容器启动脚本
# 用于启动所有服务容器

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 服务列表
SERVICES=(
  "asset-service"
  "collector-service"
  "processor-service"
  "storage-service"
  "alert-service"
  "visualization-service"
  "auth-service"
)

# 版本信息
VERSION=${VERSION:-"1.0.0"}
IMAGE_PREFIX=${IMAGE_PREFIX:-"skyi"}
NETWORK_NAME=${NETWORK_NAME:-"skyi-net"}

# 显示帮助信息
show_help() {
  echo -e "${GREEN}SKYI监控系统容器启动脚本${NC}"
  echo ""
  echo "用法: $0 [选项]"
  echo ""
  echo "选项:"
  echo "  -s, --service <服务名>    指定要启动的服务，默认启动所有服务"
  echo "  -v, --version <版本>      指定镜像版本，默认为 ${VERSION}"
  echo "  -n, --network <网络名>    指定Docker网络名称，默认为 ${NETWORK_NAME}"
  echo "  -e, --env <环境文件>      指定环境变量文件路径"
  echo "  -h, --help               显示帮助信息"
  echo ""
  echo "可用的服务:"
  for service in "${SERVICES[@]}"; do
    echo "  - $service"
  done
}

# 参数解析
SERVICE=""
ENV_FILE=""

while [[ "$#" -gt 0 ]]; do
  case $1 in
    -s|--service)
      SERVICE="$2"
      shift 2
      ;;
    -v|--version)
      VERSION="$2"
      shift 2
      ;;
    -n|--network)
      NETWORK_NAME="$2"
      shift 2
      ;;
    -e|--env)
      ENV_FILE="$2"
      shift 2
      ;;
    -h|--help)
      show_help
      exit 0
      ;;
    *)
      echo -e "${RED}未知参数: $1${NC}"
      show_help
      exit 1
      ;;
  esac
done

# 启动基础设施服务
start_infrastructure() {
  echo -e "${YELLOW}启动基础设施服务...${NC}"
  
  # 检查docker-compose是否可用
  if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}错误: docker-compose未安装或不在PATH中${NC}"
    exit 1
  fi
  
  # 检查docker-compose.yml文件是否存在
  if [ ! -f "$PROJECT_ROOT/docker/docker-compose.yml" ]; then
    echo -e "${RED}错误: docker-compose.yml文件不存在${NC}"
    exit 1
  fi
  
  # 启动基础设施服务
  cd "$PROJECT_ROOT/docker" || exit 1
  docker-compose up -d
  
  if [ $? -ne 0 ]; then
    echo -e "${RED}基础设施服务启动失败${NC}"
    exit 1
  fi
  
  # 等待基础设施服务启动完成
  echo -e "${YELLOW}等待基础设施服务启动完成...${NC}"
  sleep 20
  
  # 创建自定义网络（如果不存在）
  if ! docker network inspect ${NETWORK_NAME} &>/dev/null; then
    echo -e "${YELLOW}创建自定义网络: ${NETWORK_NAME}${NC}"
    docker network create ${NETWORK_NAME}
  fi
  
  echo -e "${GREEN}基础设施服务启动完成${NC}"
}

# 启动单个服务容器
start_service() {
  local service=$1
  echo -e "${YELLOW}启动服务: $service${NC}"
  
  local image_name="${IMAGE_PREFIX}/${service}:${VERSION}"
  local container_name="skyi-${service}"
  
  # 检查镜像是否存在
  if ! docker image inspect "$image_name" &>/dev/null; then
    echo -e "${RED}镜像不存在: $image_name${NC}"
    echo -e "${YELLOW}请先运行构建脚本: ./build-all.sh -s $service${NC}"
    return 1
  fi
  
  # 检查容器是否已存在，如果存在则停止并删除
  if docker ps -a -q -f name=${container_name} &>/dev/null; then
    echo -e "${YELLOW}停止并删除已存在的容器: ${container_name}${NC}"
    docker stop ${container_name} &>/dev/null
    docker rm ${container_name} &>/dev/null
  fi
  
  # 根据服务类型设置环境变量和端口映射
  local env_params=""
  local port_mapping=""
  
  case $service in
    asset-service)
      port_mapping="-p 8081:8081"
      ;;
    collector-service)
      port_mapping="-p 8082:8082"
      ;;
    processor-service)
      port_mapping="-p 8083:8083"
      ;;
    storage-service)
      port_mapping="-p 8084:8084"
      ;;
    alert-service)
      port_mapping="-p 8085:8085"
      ;;
    visualization-service)
      port_mapping="-p 8086:8086"
      ;;
    auth-service)
      port_mapping="-p 8087:8087"
      ;;
  esac
  
  # 如果指定了环境变量文件，则添加到启动参数中
  if [ -n "$ENV_FILE" ] && [ -f "$ENV_FILE" ]; then
    env_params="--env-file $ENV_FILE"
  fi
  
  # 启动服务容器
  docker run -d \
    --name ${container_name} \
    --network ${NETWORK_NAME} \
    ${port_mapping} \
    ${env_params} \
    -e MYSQL_HOST=skyi-mysql \
    -e REDIS_HOST=skyi-redis \
    -e INFLUXDB_HOST=skyi-influxdb \
    -e NACOS_HOST=skyi-nacos \
    -e KAFKA_HOST=skyi-kafka \
    ${image_name}
  
  if [ $? -ne 0 ]; then
    echo -e "${RED}服务启动失败: $service${NC}"
    return 1
  fi
  
  echo -e "${GREEN}服务启动成功: $service${NC}"
  return 0
}

# 主函数
main() {
  echo -e "${GREEN}===== SKYI 监控系统容器启动脚本 =====${NC}"
  echo -e "版本: ${VERSION}"
  
  # 检查docker命令是否可用
  if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker未安装或不在PATH中${NC}"
    exit 1
  fi
  
  # 启动基础设施服务
  start_infrastructure
  
  # 如果指定了服务，则只启动指定的服务
  if [ -n "$SERVICE" ]; then
    if [[ " ${SERVICES[*]} " == *" $SERVICE "* ]]; then
      start_service "$SERVICE"
      exit $?
    else
      echo -e "${RED}未知服务: $SERVICE${NC}"
      show_help
      exit 1
    fi
  fi
  
  # 启动所有服务
  for service in "${SERVICES[@]}"; do
    start_service "$service"
    if [ $? -ne 0 ]; then
      echo -e "${RED}启动失败: $service${NC}"
    fi
  done
  
  echo -e "${GREEN}所有服务启动完成${NC}"
  echo -e "${GREEN}可以通过以下地址访问各个服务:${NC}"
  echo -e "${GREEN}资产管理服务: http://localhost:8081${NC}"
  echo -e "${GREEN}采集服务: http://localhost:8082${NC}"
  echo -e "${GREEN}处理服务: http://localhost:8083${NC}"
  echo -e "${GREEN}存储服务: http://localhost:8084${NC}"
  echo -e "${GREEN}告警服务: http://localhost:8085${NC}"
  echo -e "${GREEN}可视化服务: http://localhost:8086${NC}"
  echo -e "${GREEN}认证服务: http://localhost:8087${NC}"
  echo -e "${GREEN}Nacos控制台: http://localhost:8848/nacos${NC}"
  echo -e "${GREEN}InfluxDB控制台: http://localhost:8086${NC}"
}

# 执行主函数
main 