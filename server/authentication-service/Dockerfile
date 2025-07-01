FROM maven:3.8-amazoncorretto-21 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim

COPY --from=builder /app/target/*.jar /authentication-service.jar

LABEL authors="alexandre.messuves@gmail.com"

ENTRYPOINT ["java", "-jar", "authentication-service.jar"]