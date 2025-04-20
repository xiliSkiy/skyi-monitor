#!/bin/bash

echo "初始化采集服务数据库..."

# 检查MySQL容器是否存在
MYSQL_CONTAINER=$(docker ps -q -f name=skyi-mysql)

if [ -z "$MYSQL_CONTAINER" ]; then
    echo "错误: MySQL容器不存在，请先启动MySQL容器"
    exit 1
fi

# 将SQL文件复制到容器
echo "将初始化SQL脚本复制到MySQL容器..."
docker cp "$(dirname "$0")/init.sql" $MYSQL_CONTAINER:/tmp/collector-init.sql

# 在容器中执行SQL脚本
echo "执行SQL初始化脚本..."
docker exec $MYSQL_CONTAINER bash -c "mysql -uroot -p123456 < /tmp/collector-init.sql"

if [ $? -eq 0 ]; then
    echo "采集服务数据库初始化成功！"
else
    echo "采集服务数据库初始化失败！"
    exit 1
fi 