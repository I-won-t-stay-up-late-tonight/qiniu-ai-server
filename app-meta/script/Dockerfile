FROM eclipse-temurin:21-jdk-alpine

# 设置时区为上海（Alpine Linux 方式）
RUN apk add --no-cache tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 创建应用目录和临时音频目录
RUN mkdir -p /app/temp-audio /config

# 复制JAR文件
COPY ./*.jar /app.jar

EXPOSE 9002

# 配置文件卷
VOLUME /config /app/temp-audio /app/logs

# 使用更安全的启动方式
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.config.location=file:/config/application-dev.yml"]