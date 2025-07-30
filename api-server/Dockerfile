# Dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/api-server.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
