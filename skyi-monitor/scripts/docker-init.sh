#!/bin/bash
set -e

# SKYI监控系统通用初始化脚本
# 用于容器中应用的启动前准备工作

# 打印彩色日志
print_info() {
  echo -e "\033[0;34m[INFO] $1\033[0m"
}

print_success() {
  echo -e "\033[0;32m[SUCCESS] $1\033[0m"
}

print_error() {
  echo -e "\033[0;31m[ERROR] $1\033[0m"
}

print_warn() {
  echo -e "\033[0;33m[WARN] $1\033[0m"
}

# 等待服务可用
wait_for_service() {
  local host=$1
  local port=$2
  local service_name=$3
  local max_attempts=${4:-30}
  local retry_interval=${5:-2}
  
  print_info "等待 $service_name ($host:$port) 服务就绪..."
  
  local attempt=1
  while [ $attempt -le $max_attempts ]; do
    if nc -z $host $port > /dev/null 2>&1; then
      print_success "$service_name 已就绪"
      return 0
    fi
    
    print_warn "$service_name 未就绪，第 $attempt 次尝试，等待 $retry_interval 秒..."
    sleep $retry_interval
    attempt=$((attempt + 1))
  done
  
  print_error "$service_name 服务在 $max_attempts 次尝试后仍然不可用"
  return 1
}

# 等待MySQL数据库就绪
wait_for_mysql() {
  local host=${MYSQL_HOST:-localhost}
  local port=${MYSQL_PORT:-3306}
  local user=${MYSQL_USER:-root}
  local password=${MYSQL_PASSWORD:-123456}
  local database=${MYSQL_DATABASE}
  local max_attempts=${MYSQL_MAX_ATTEMPTS:-30}
  local retry_interval=${MYSQL_RETRY_INTERVAL:-2}
  
  print_info "等待 MySQL 数据库连接..."
  
  wait_for_service $host $port "MySQL" $max_attempts $retry_interval
  
  if [ $? -ne 0 ]; then
    print_error "无法连接到 MySQL 数据库"
    return 1
  fi
  
  if [ -n "$database" ]; then
    print_info "检查数据库 $database 是否存在..."
    
    mysql -h $host -P $port -u $user -p$password -e "CREATE DATABASE IF NOT EXISTS $database DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
    
    if [ $? -eq 0 ]; then
      print_success "数据库 $database 已就绪"
    else
      print_error "创建数据库 $database 失败"
      return 1
    fi
  fi
  
  return 0
}

# 等待Nacos服务就绪
wait_for_nacos() {
  local host=${NACOS_HOST:-localhost}
  local port=${NACOS_PORT:-8848}
  local max_attempts=${NACOS_MAX_ATTEMPTS:-60}
  local retry_interval=${NACOS_RETRY_INTERVAL:-2}
  
  wait_for_service $host $port "Nacos" $max_attempts $retry_interval
  
  if [ $? -ne 0 ]; then
    print_error "无法连接到 Nacos 服务"
    return 1
  fi
  
  # 检查Nacos健康状态API
  print_info "检查 Nacos 服务健康状态..."
  local attempt=1
  while [ $attempt -le $max_attempts ]; do
    local response=$(curl -s -w "%{http_code}" -o /dev/null http://$host:$port/nacos/v1/console/health)
    
    if [ "$response" = "200" ]; then
      print_success "Nacos 服务健康状态检查通过"
      return 0
    fi
    
    print_warn "Nacos 服务尚未就绪，第 $attempt 次尝试，等待 $retry_interval 秒..."
    sleep $retry_interval
    attempt=$((attempt + 1))
  done
  
  print_error "Nacos 服务健康状态检查在 $max_attempts 次尝试后仍然不通过"
  return 1
}

# 等待Redis服务就绪
wait_for_redis() {
  local host=${REDIS_HOST:-localhost}
  local port=${REDIS_PORT:-6379}
  local password=${REDIS_PASSWORD}
  local max_attempts=${REDIS_MAX_ATTEMPTS:-30}
  local retry_interval=${REDIS_RETRY_INTERVAL:-2}
  
  wait_for_service $host $port "Redis" $max_attempts $retry_interval
  
  if [ $? -ne 0 ]; then
    print_error "无法连接到 Redis 服务"
    return 1
  fi
  
  # 如果设置了密码，执行简单的Redis命令来确认连接有效
  if [ -n "$password" ]; then
    print_info "测试 Redis 连接..."
    
    if ! redis-cli -h $host -p $port -a $password ping > /dev/null 2>&1; then
      print_error "Redis 连接测试失败"
      return 1
    fi
  fi
  
  print_success "Redis 服务已就绪"
  return 0
}

# 等待Kafka服务就绪
wait_for_kafka() {
  local host=${KAFKA_HOST:-localhost}
  local port=${KAFKA_PORT:-9092}
  local max_attempts=${KAFKA_MAX_ATTEMPTS:-30}
  local retry_interval=${KAFKA_RETRY_INTERVAL:-2}
  
  wait_for_service $host $port "Kafka" $max_attempts $retry_interval
  
  if [ $? -ne 0 ]; then
    print_error "无法连接到 Kafka 服务"
    return 1
  fi
  
  print_success "Kafka 服务已就绪"
  return 0
}

# 等待InfluxDB服务就绪
wait_for_influxdb() {
  local host=${INFLUXDB_HOST:-localhost}
  local port=${INFLUXDB_PORT:-8086}
  local max_attempts=${INFLUXDB_MAX_ATTEMPTS:-30}
  local retry_interval=${INFLUXDB_RETRY_INTERVAL:-2}
  
  wait_for_service $host $port "InfluxDB" $max_attempts $retry_interval
  
  if [ $? -ne 0 ]; then
    print_error "无法连接到 InfluxDB 服务"
    return 1
  fi
  
  print_success "InfluxDB 服务已就绪"
  return 0
}

# 应用初始化
init_application() {
  local app_name=$1
  
  print_info "开始初始化 $app_name 应用..."
  
  # 检查是否存在SQL初始化脚本
  if [ -f "/app/init.sql" ]; then
    print_info "检测到初始化SQL脚本，执行数据库初始化..."
    
    local mysql_host=${MYSQL_HOST:-localhost}
    local mysql_port=${MYSQL_PORT:-3306}
    local mysql_user=${MYSQL_USER:-root}
    local mysql_password=${MYSQL_PASSWORD:-123456}
    local mysql_database=${MYSQL_DATABASE}
    
    if [ -n "$mysql_database" ]; then
      mysql -h $mysql_host -P $mysql_port -u $mysql_user -p$mysql_password $mysql_database < /app/init.sql
      
      if [ $? -eq 0 ]; then
        print_success "数据库初始化完成"
      else
        print_error "数据库初始化失败"
        return 1
      fi
    fi
  fi
  
  print_success "$app_name 应用初始化完成"
  return 0
}

# 根据参数执行相应的初始化操作
init_service() {
  local service_name=$1
  print_info "初始化服务: $service_name"
  
  # 等待基础设施服务就绪
  case $service_name in
    asset-service)
      wait_for_mysql && wait_for_nacos
      ;;
    collector-service)
      wait_for_mysql && wait_for_nacos && wait_for_kafka
      ;;
    processor-service)
      wait_for_mysql && wait_for_nacos && wait_for_kafka && wait_for_influxdb
      ;;
    storage-service)
      wait_for_mysql && wait_for_nacos && wait_for_influxdb && wait_for_redis
      ;;
    alert-service)
      wait_for_mysql && wait_for_nacos && wait_for_kafka && wait_for_redis
      ;;
    visualization-service)
      wait_for_mysql && wait_for_nacos && wait_for_influxdb
      ;;
    auth-service)
      wait_for_mysql && wait_for_nacos && wait_for_redis
      ;;
    *)
      print_warn "未知服务类型: $service_name，仅等待基础设施服务"
      wait_for_mysql
      wait_for_nacos
      ;;
  esac
  
  if [ $? -ne 0 ]; then
    print_error "服务依赖检查失败"
    return 1
  fi
  
  # 执行应用特定的初始化
  init_application $service_name
  
  return $?
}

# 主函数
main() {
  local service_name=${SERVICE_NAME:-unknown}
  
  # 打印系统信息
  print_info "===== SKYI 监控系统启动初始化脚本 ====="
  print_info "服务名称: $service_name"
  print_info "JDK版本: $(java -version 2>&1 | head -n 1)"
  print_info "主机名: $(hostname)"
  print_info "======================================"
  
  # 初始化服务
  init_service $service_name
  
  if [ $? -ne 0 ]; then
    print_error "$service_name 初始化失败，无法启动应用"
    exit 1
  fi
  
  print_success "$service_name 初始化完成，准备启动应用"
  return 0
}

# 执行主函数
main "$@" 