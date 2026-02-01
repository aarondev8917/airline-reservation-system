# =============================================================================
# Airline Reservation System - Multi-stage Dockerfile
# =============================================================================
# Build stage: Maven + Java 21
# Run stage: JRE 21 slim
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build
# -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom first for layer caching
COPY pom.xml .

# Download dependencies (cached unless pom changes)
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# -----------------------------------------------------------------------------
# Stage 2: Run
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create non-root user
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy built JAR from builder
COPY --from=builder /app/target/airline-reservation-system-1.0.0.jar app.jar

# Switch to non-root user
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

# Override DB/Redis via env vars in docker-compose or run command:
#   SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
#   SPRING_DATA_REDIS_HOST, SPRING_DATA_REDIS_PORT
#   AVIATIONSTACK_API_KEY, APP_JWT_SECRET
ENTRYPOINT ["java", "-jar", "app.jar"]
