FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/chat-server.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
