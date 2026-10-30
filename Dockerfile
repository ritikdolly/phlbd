# Multi-stage Docker build for Savoria Restaurant Backend
# Stage 1: Build JAR
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal Distroless / Temurin JRE runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Run as non-root user for enterprise container security
RUN groupadd -r savoria && useradd -r -g savoria savoria
USER savoria

COPY --from=builder /workspace/app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

