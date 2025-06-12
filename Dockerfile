FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/your-app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]