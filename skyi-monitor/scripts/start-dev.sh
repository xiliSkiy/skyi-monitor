#!/bin/bash

# 启动开发环境

echo "启动开发环境..."

# 检查Docker是否安装
if ! [ -x "$(command -v docker)" ]; then
  echo '错误: Docker 未安装.' >&2
  exit 1
fi

# 检查Docker Compose是否安装
if ! [ -x "$(command -v docker-compose)" ]; then
  echo '错误: Docker Compose 未安装.' >&2
  exit 1
fi

# 启动基础设施服务
echo "启动基础设施服务..."

# 创建网络
docker network create skyi-net 2>/dev/null || true

# 启动MySQL
echo "启动MySQL..."
docker run -d --name skyi-mysql \
  --network skyi-net \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  mysql:8.0

# 等待MySQL启动完成
echo "等待MySQL启动完成..."
sleep 10

# 创建所需的数据库
echo "创建数据库..."
docker exec skyi-mysql mysql -uroot -p123456 -e "CREATE DATABASE IF NOT EXISTS skyi_asset DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
docker exec skyi-mysql mysql -uroot -p123456 -e "CREATE DATABASE IF NOT EXISTS skyi_collector DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
docker exec skyi-mysql mysql -uroot -p123456 -e "CREATE DATABASE IF NOT EXISTS skyi_alert DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
docker exec skyi-mysql mysql -uroot -p123456 -e "CREATE DATABASE IF NOT EXISTS skyi_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 初始化数据库表结构
echo "初始化数据库表结构..."
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 拷贝SQL初始化脚本到容器
docker cp "${SCRIPT_DIR}/init-collector-db.sql" skyi-mysql:/tmp/init-collector-db.sql

# 执行SQL初始化脚本
docker exec skyi-mysql mysql -uroot -p123456 -e "source /tmp/init-collector-db.sql"

# 启动Redis
echo "启动Redis..."
docker run -d --name skyi-redis \
  --network skyi-net \
  -p 6379:6379 \
  redis:6.2

# 启动InfluxDB
echo "启动InfluxDB..."
docker run -d --name skyi-influxdb \
  --network skyi-net \
  -p 8086:8086 \
  -e DOCKER_INFLUXDB_INIT_MODE=setup \
  -e DOCKER_INFLUXDB_INIT_USERNAME=admin \
  -e DOCKER_INFLUXDB_INIT_PASSWORD=Skyi@Influx2024 \
  -e DOCKER_INFLUXDB_INIT_ORG=skyi \
  -e DOCKER_INFLUXDB_INIT_BUCKET=monitoring \
  -e DOCKER_INFLUXDB_INIT_ADMIN_TOKEN=skyi-token \
  -v influxdb-data:/var/lib/influxdb2 \
  influxdb:2.6

# 启动Nacos
echo "启动Nacos..."
docker run -d --name skyi-nacos \
  --network skyi-net \
  -p 8848:8848 \
  -e MODE=standalone \
  -e PREFER_HOST_MODE=hostname \
  nacos/nacos-server:v2.2.0

# 启动Kafka
echo "启动Kafka..."
docker run -d --name skyi-zookeeper \
  --network skyi-net \
  -p 2181:2181 \
  zookeeper:3.7

docker run -d --name skyi-kafka \
  --network skyi-net \
  -p 9092:9092 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=skyi-zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:7.0.0

echo "基础设施服务启动完成！"
echo "请等待所有服务就绪后再启动应用服务..." 