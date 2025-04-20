FROM openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 安装必要工具
RUN apt-get update && apt-get install -y curl netcat-openbsd mysql-client && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 设置默认环境变量
ENV JAVA_OPTS="-Xms512m -Xmx1024m" \
    SERVICE_NAME="visualization-service" \
    NACOS_HOST="localhost" \
    NACOS_PORT="8848" \
    MYSQL_HOST="localhost" \
    MYSQL_PORT="3306" \
    MYSQL_DATABASE="skyi_visualization" \
    MYSQL_USER="root" \
    MYSQL_PASSWORD="123456" \
    INFLUXDB_HOST="localhost" \
    INFLUXDB_PORT="8086" \
    SERVER_PORT="8086" \
    TZ="Asia/Shanghai"

# 设置时区
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制启动和初始化脚本
COPY docker-entrypoint.sh /app/
COPY ../../scripts/docker-init.sh /app/

# 赋予脚本执行权限
RUN chmod +x /app/docker-entrypoint.sh /app/docker-init.sh

# 复制应用JAR包
COPY target/visualization-service-*.jar /app/app.jar

# 暴露端口
EXPOSE ${SERVER_PORT}

# 启动应用
ENTRYPOINT ["/app/docker-entrypoint.sh"] 