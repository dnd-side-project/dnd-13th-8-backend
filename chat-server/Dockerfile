FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY build/libs/chat-server.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
