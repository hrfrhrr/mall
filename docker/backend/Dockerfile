# 后端服务统一镜像：宿主机构建好 Jar 后，仅做运行时封装，构建极快
ARG JAR_FILE=baizhan-gateway/target/baizhan-gateway-1.0.0.jar
FROM eclipse-temurin:17-jre-jammy

LABEL maintainer="baizhan-mall"

WORKDIR /app

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

# 容器内以非 root 运行（-m 必须创建家目录，Dubbo 等组件需在 ~/.dubbo 写缓存）
RUN useradd -m -u 1000 baizhan && chown -R baizhan:baizhan /app
USER baizhan

ENV TZ=Asia/Shanghai
EXPOSE 8080

# 健康检查（JDK17 无 curl，使用 jar 内 actuator 亦可，这里用进程探测兜底）
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD kill -0 1 || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
