#!/bin/bash

# 可视化服务开发环境启动脚本

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# 切换到项目目录
cd "$SCRIPT_DIR"

# 设置Java 11环境
export JAVA_HOME=$(/usr/libexec/java_home -v 11)

# 打印环境信息
echo "=== 环境信息 ==="
echo "Java版本: $(java -version 2>&1 | head -n 1)"
echo "项目目录: $SCRIPT_DIR"
echo "=================="

# 使用Maven编译
echo "编译项目..."
mvn clean package -DskipTests

# 检查编译结果
if [ $? -ne 0 ]; then
    echo "编译失败！"
    exit 1
fi

echo "编译成功！"

# 提示选择运行模式
echo "请选择运行模式:"
echo "1. 开发模式（使用实际InfluxDB）"
echo "2. 模拟模式（使用模拟数据）"
read -p "请输入选项 (1/2): " option

case $option in
    1)
        echo "以开发模式启动服务（连接实际的InfluxDB）..."
        java -jar target/visualization-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
        ;;
    2)
        echo "以模拟模式启动服务（使用模拟数据）..."
        java -jar target/visualization-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev-mock
        ;;
    *)
        echo "无效选项，默认使用开发模式启动..."
        java -jar target/visualization-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
        ;;
esac 