#!/bin/bash

# 设置变量
BOOTSTRAP_SERVER="localhost:9092"
METRIC_DATA_TOPIC="collector-metric-data"
COLLECTION_STATUS_TOPIC="collector-status"
PARTITION_COUNT=3
REPLICATION_FACTOR=1

echo "开始创建Kafka主题..."

# 检查Kafka是否可用
echo "检查Kafka服务状态..."
echo "尝试连接到 $BOOTSTRAP_SERVER..."
timeout 5 bash -c "cat < /dev/null > /dev/tcp/localhost/9092"
if [ $? -ne 0 ]; then
    echo "错误: 无法连接到Kafka服务，请确保Kafka已经启动。"
    exit 1
fi

# 创建指标数据主题
echo "创建指标数据主题: $METRIC_DATA_TOPIC"
docker exec skyi-kafka kafka-topics --create \
    --bootstrap-server $BOOTSTRAP_SERVER \
    --topic $METRIC_DATA_TOPIC \
    --partitions $PARTITION_COUNT \
    --replication-factor $REPLICATION_FACTOR \
    --config cleanup.policy=compact \
    --if-not-exists

if [ $? -eq 0 ]; then
    echo "主题 $METRIC_DATA_TOPIC 创建成功或已存在。"
else
    echo "错误: 创建主题 $METRIC_DATA_TOPIC 失败。"
fi

# 创建采集状态主题
echo "创建采集状态主题: $COLLECTION_STATUS_TOPIC"
docker exec skyi-kafka kafka-topics --create \
    --bootstrap-server $BOOTSTRAP_SERVER \
    --topic $COLLECTION_STATUS_TOPIC \
    --partitions $PARTITION_COUNT \
    --replication-factor $REPLICATION_FACTOR \
    --if-not-exists

if [ $? -eq 0 ]; then
    echo "主题 $COLLECTION_STATUS_TOPIC 创建成功或已存在。"
else
    echo "错误: 创建主题 $COLLECTION_STATUS_TOPIC 失败。"
fi

# 列出所有主题
echo "已创建的主题列表:"
docker exec skyi-kafka kafka-topics --list --bootstrap-server $BOOTSTRAP_SERVER

echo "主题创建完成。" 