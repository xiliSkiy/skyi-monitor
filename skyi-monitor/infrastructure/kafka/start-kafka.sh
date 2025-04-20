#!/bin/bash

# 设置变量
KAFKA_HOME=$(dirname "$0")
DOCKER_COMPOSE_FILE="$KAFKA_HOME/docker/docker-compose.yml"

echo "Starting Kafka and Zookeeper services..."

# 创建数据目录
mkdir -p $KAFKA_HOME/data/kafka
mkdir -p $KAFKA_HOME/data/zookeeper/data
mkdir -p $KAFKA_HOME/data/zookeeper/datalog

# 启动docker-compose
cd $KAFKA_HOME && docker-compose -f $DOCKER_COMPOSE_FILE up -d

# 检查服务是否启动成功
if [ $? -eq 0 ]; then
    echo "Kafka services started successfully."
    echo "Kafka: localhost:9092"
    echo "Zookeeper: localhost:2181"
    echo "Kafka UI: http://localhost:8080"
else
    echo "Failed to start Kafka services. Please check the logs."
    exit 1
fi 