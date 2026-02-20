# Étape 1 : Construction avec Maven et Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Exécution avec Java 21 (image slim)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/STOCK-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
