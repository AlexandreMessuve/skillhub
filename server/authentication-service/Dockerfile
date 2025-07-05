FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

COPY --from=builder /app/target/*.jar /authentication-service.jar

LABEL authors="alexandre.messuves@gmail.com"

ENTRYPOINT ["java", "-jar", "authentication-service.jar"]