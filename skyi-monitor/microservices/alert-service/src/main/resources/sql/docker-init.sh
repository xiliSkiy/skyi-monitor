#!/bin/bash
# 告警服务数据库初始化脚本 - Docker版本

# 获取MySQL容器名称（可以根据实际情况修改）
MYSQL_CONTAINER=$(docker ps | grep mysql | awk '{print $NF}')

if [ -z "$MYSQL_CONTAINER" ]; then
    echo "错误：找不到MySQL容器，请确保MySQL容器已启动"
    exit 1
fi

# 获取当前脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

echo "找到MySQL容器: $MYSQL_CONTAINER"
echo "正在初始化告警服务数据库..."

# 将SQL脚本复制到容器内
docker cp "$SCRIPT_DIR/init.sql" "$MYSQL_CONTAINER:/tmp/alert-init.sql"

# 在容器内执行SQL脚本
docker exec -i "$MYSQL_CONTAINER" bash -c "mysql -uroot -p123456 < /tmp/alert-init.sql"

# 检查执行结果
if [ $? -eq 0 ]; then
    echo "告警服务数据库初始化成功！"
else
    echo "错误：告警服务数据库初始化失败！"
    exit 1
fi

# 清理临时文件
docker exec -i "$MYSQL_CONTAINER" bash -c "rm -f /tmp/alert-init.sql"

echo "初始化完成。" 