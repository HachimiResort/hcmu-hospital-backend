# 使用与你项目匹配的 Java 17 基础镜像
FROM eclipse-temurin:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 将构建好的主模块 JAR 文件复制到容器中
# !! 注意：这里的路径是 HCMU-server/target/*.jar，因为你的可执行 JAR 在子模块里 !!
COPY HCMU-server/target/*.jar app.jar

# 暴露应用程序的端口 (与你的 env.PORT 保持一致)
EXPOSE 8080

# 容器启动时执行的命令
# Spring Boot 3+ 的默认 fat jar 启动方式
ENTRYPOINT ["java", "-jar", "app.jar"]
