#!/bin/bash

# SKYI监控系统构建脚本
# 用于构建所有服务Docker镜像

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
VERSION="1.0.0"
IMAGE_PREFIX="skyi"

# 显示帮助信息
show_help() {
  echo -e "${GREEN}SKYI监控系统构建和部署脚本${NC}"
  echo ""
  echo "用法: $0 [选项]"
  echo ""
  echo "选项:"
  echo "  -s, --service <服务名>    指定要构建的服务，默认构建所有服务"
  echo "  -v, --version <版本>      指定构建版本，默认为 ${VERSION}"
  echo "  -p, --push               构建后推送镜像到仓库"
  echo "  -h, --help               显示帮助信息"
  echo ""
  echo "可用的服务:"
  for service in "${SERVICES[@]}"; do
    echo "  - $service"
  done
}

# 参数解析
SERVICE=""
PUSH_IMAGE=false

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
    -p|--push)
      PUSH_IMAGE=true
      shift
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

# 构建单个服务
build_service() {
  local service=$1
  echo -e "${YELLOW}开始构建 $service...${NC}"
  
  local service_dir="$PROJECT_ROOT/microservices/$service"
  if [ ! -d "$service_dir" ]; then
    echo -e "${RED}服务目录不存在: $service_dir${NC}"
    return 1
  fi
  
  cd "$service_dir" || return 1
  
  # 确保docker-init.sh可被复制
  if [ ! -f "$SCRIPT_DIR/docker-init.sh" ]; then
    echo -e "${RED}初始化脚本不存在: $SCRIPT_DIR/docker-init.sh${NC}"
    return 1
  fi
  
  # 检查是否存在Dockerfile
  if [ ! -f "Dockerfile" ]; then
    echo -e "${RED}Dockerfile不存在: $service_dir/Dockerfile${NC}"
    return 1
  fi
  
  # 构建Java应用
  echo -e "${YELLOW}编译 $service...${NC}"
  mvn clean package -DskipTests
  
  if [ $? -ne 0 ]; then
    echo -e "${RED}$service 编译失败${NC}"
    return 1
  fi
  
  # 构建Docker镜像
  local image_name="${IMAGE_PREFIX}/${service}:${VERSION}"
  echo -e "${YELLOW}构建Docker镜像: $image_name${NC}"
  
  docker build -t "$image_name" .
  
  if [ $? -ne 0 ]; then
    echo -e "${RED}$service Docker镜像构建失败${NC}"
    return 1
  fi
  
  # 是否推送镜像
  if $PUSH_IMAGE; then
    echo -e "${YELLOW}推送Docker镜像: $image_name${NC}"
    docker push "$image_name"
    
    if [ $? -ne 0 ]; then
      echo -e "${RED}$service Docker镜像推送失败${NC}"
      return 1
    fi
  fi
  
  echo -e "${GREEN}$service 构建完成${NC}"
  return 0
}

# 主函数
main() {
  echo -e "${GREEN}===== SKYI 监控系统构建脚本 =====${NC}"
  echo -e "版本: ${VERSION}"
  
  # 检查docker命令是否可用
  if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker未安装或不在PATH中${NC}"
    exit 1
  fi
  
  # 检查maven命令是否可用
  if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: Maven未安装或不在PATH中${NC}"
    exit 1
  fi
  
  # 首先构建common模块
  echo -e "${YELLOW}构建公共模块...${NC}"
  cd "$PROJECT_ROOT/common" || exit 1
  mvn clean install -DskipTests
  
  if [ $? -ne 0 ]; then
    echo -e "${RED}公共模块构建失败${NC}"
    exit 1
  fi
  
  # 如果指定了服务，则只构建指定的服务
  if [ -n "$SERVICE" ]; then
    if [[ " ${SERVICES[*]} " == *" $SERVICE "* ]]; then
      build_service "$SERVICE"
      exit $?
    else
      echo -e "${RED}未知服务: $SERVICE${NC}"
      show_help
      exit 1
    fi
  fi
  
  # 构建所有服务
  for service in "${SERVICES[@]}"; do
    build_service "$service"
    if [ $? -ne 0 ]; then
      echo -e "${RED}构建失败，中止后续构建${NC}"
      exit 1
    fi
  done
  
  echo -e "${GREEN}所有服务构建完成${NC}"
}

# 执行主函数
main 