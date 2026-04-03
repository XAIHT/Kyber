# syntax=docker/dockerfile:1

FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package

FROM payara/server-web:6.2025.11-jdk21
COPY --from=build /workspace/target/XaihtKyber.war $DEPLOY_DIR/XaihtKyber.war

EXPOSE 8080
