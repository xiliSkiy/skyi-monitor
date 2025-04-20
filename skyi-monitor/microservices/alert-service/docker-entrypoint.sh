#!/bin/bash
set -e

# 告警服务启动脚本

# 执行通用初始化脚本
/app/docker-init.sh

# 设置Java启动参数
JAVA_OPTS="${JAVA_OPTS} -Dserver.port=${SERVER_PORT}"
JAVA_OPTS="${JAVA_OPTS} -Dspring.datasource.url=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai"
JAVA_OPTS="${JAVA_OPTS} -Dspring.datasource.username=${MYSQL_USER}"
JAVA_OPTS="${JAVA_OPTS} -Dspring.datasource.password=${MYSQL_PASSWORD}"
JAVA_OPTS="${JAVA_OPTS} -Dspring.cloud.nacos.discovery.server-addr=${NACOS_HOST}:${NACOS_PORT}"
JAVA_OPTS="${JAVA_OPTS} -Dspring.cloud.nacos.config.server-addr=${NACOS_HOST}:${NACOS_PORT}"
JAVA_OPTS="${JAVA_OPTS} -Dspring.redis.host=${REDIS_HOST}"
JAVA_OPTS="${JAVA_OPTS} -Dspring.redis.port=${REDIS_PORT}"
JAVA_OPTS="${JAVA_OPTS} -Dspring.kafka.bootstrap-servers=${KAFKA_HOST}:${KAFKA_PORT}"

# 打印启动参数
echo "=== 启动告警服务 ==="
echo "服务端口: ${SERVER_PORT}"
echo "数据库地址: ${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}"
echo "Nacos地址: ${NACOS_HOST}:${NACOS_PORT}"
echo "Redis地址: ${REDIS_HOST}:${REDIS_PORT}"
echo "Kafka地址: ${KAFKA_HOST}:${KAFKA_PORT}"
echo "================================"

# 启动Java应用
exec java ${JAVA_OPTS} -jar /app/app.jar 