FROM maven:3-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package -DskipTests
FROM eclipse-temurin:17-alpine
COPY --from=build /target/*.jar OnlineExpenseTracker-2-0.0.1-SNAPSHOT.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
