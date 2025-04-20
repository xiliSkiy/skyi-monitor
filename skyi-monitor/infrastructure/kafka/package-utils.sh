#!/bin/bash

# 设置变量
KAFKA_HOME=$(dirname "$0")
UTILS_DIR="$KAFKA_HOME/utils"
TEMP_DIR="$KAFKA_HOME/temp"
OUTPUT_DIR="$KAFKA_HOME/target"
PACKAGE_NAME="kafka-utils"
VERSION="1.0.0"

echo "开始打包Kafka测试工具..."

# 创建临时目录和输出目录
mkdir -p $TEMP_DIR/com/skyi/infrastructure/kafka/utils
mkdir -p $OUTPUT_DIR

# 复制Java源文件到临时目录
cp $UTILS_DIR/*.java $TEMP_DIR/com/skyi/infrastructure/kafka/utils/

# 下载依赖jar包
echo "下载依赖包..."
mkdir -p $TEMP_DIR/lib
curl -s -o $TEMP_DIR/lib/kafka-clients-3.0.0.jar https://repo1.maven.org/maven2/org/apache/kafka/kafka-clients/3.0.0/kafka-clients-3.0.0.jar
curl -s -o $TEMP_DIR/lib/fastjson-1.2.83.jar https://repo1.maven.org/maven2/com/alibaba/fastjson/1.2.83/fastjson-1.2.83.jar
curl -s -o $TEMP_DIR/lib/slf4j-api-1.7.36.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar
curl -s -o $TEMP_DIR/lib/slf4j-simple-1.7.36.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar

# 创建classpath
CLASSPATH=""
for jar in $TEMP_DIR/lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done
CLASSPATH=${CLASSPATH:1}  # 移除第一个冒号

# 编译Java文件
echo "编译Java文件..."
javac -cp $CLASSPATH -d $TEMP_DIR $TEMP_DIR/com/skyi/infrastructure/kafka/utils/*.java

# 如果编译失败则退出
if [ $? -ne 0 ]; then
    echo "编译失败。"
    exit 1
fi

# 创建运行脚本
echo "创建运行脚本..."

# 生产者脚本
cat > $TEMP_DIR/run-producer.sh << EOF
#!/bin/bash
SCRIPT_DIR=\$(dirname "\$0")
java -cp "\$SCRIPT_DIR:\$SCRIPT_DIR/lib/*" com.skyi.infrastructure.kafka.utils.KafkaProducerTester \$@
EOF

# 消费者脚本
cat > $TEMP_DIR/run-consumer.sh << EOF
#!/bin/bash
SCRIPT_DIR=\$(dirname "\$0")
java -cp "\$SCRIPT_DIR:\$SCRIPT_DIR/lib/*" com.skyi.infrastructure.kafka.utils.KafkaConsumerTester \$@
EOF

# 设置可执行权限
chmod +x $TEMP_DIR/run-producer.sh $TEMP_DIR/run-consumer.sh

# 创建README
cat > $TEMP_DIR/README.txt << EOF
Kafka测试工具 v${VERSION}
==============

本工具包含Kafka生产者和消费者测试程序。

使用方法:
1. 启动Kafka生产者:
   ./run-producer.sh

2. 启动Kafka消费者:
   ./run-consumer.sh

注意: 确保Kafka服务已经启动，并且可以通过localhost:9092访问。
EOF

# 打包
echo "创建ZIP包..."
cd $TEMP_DIR
zip -r "$OUTPUT_DIR/${PACKAGE_NAME}-${VERSION}.zip" ./*

# 清理临时目录
echo "清理临时文件..."
rm -rf $TEMP_DIR

echo "打包完成: $OUTPUT_DIR/${PACKAGE_NAME}-${VERSION}.zip" 