#!/bin/bash

# 设置变量
KAFKA_HOME=$(dirname "$0")
DOCKER_COMPOSE_FILE="$KAFKA_HOME/docker/docker-compose.yml"

echo "Stopping Kafka and Zookeeper services..."

# 停止docker-compose
cd $KAFKA_HOME && docker-compose -f $DOCKER_COMPOSE_FILE down

# 检查服务是否停止成功
if [ $? -eq 0 ]; then
    echo "Kafka services stopped successfully."
else
    echo "Failed to stop Kafka services. Please check the logs."
    exit 1
fi 