# 使用多阶段构建
# 1. 构建阶段
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
# 复制源码
COPY src ./src
# 打包 (跳过测试)
RUN mvn clean package -DskipTests

# 2. 运行阶段
FROM openjdk:17-jdk-slim
WORKDIR /app
# 从构建阶段复制 jar 包
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8079

# 启动命令 (支持 JVM 参数和 Spring 参数)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
