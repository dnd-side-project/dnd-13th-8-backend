# Dockerfile
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY build/libs/api-server.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
