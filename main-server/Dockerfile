# 1. JDK 21 eclipse 이미지 사용
FROM eclipse-temurin:21-jdk

# 2. 작업 디렉토리 생성
WORKDIR /app

# 3. 빌드된 jar 파일 복사
COPY build/libs/main-server.jar app.jar

# 4. 내부에서 사용할 포트 (예: 8083)
EXPOSE 8083

# 5. 컨테이너 실행 시 jar 파일 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
