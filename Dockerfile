# Multi-stage build for Spring Boot application
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Create uploads and tickets directories
RUN mkdir -p /tmp/uploads /tmp/tickets

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create non-root user for security
RUN addgroup -g 1001 -S springboot && \
    adduser -S springboot -u 1001 -G springboot

# Change ownership of necessary directories
RUN chown -R springboot:springboot /app /tmp/uploads /tmp/tickets

# Switch to non-root user
USER springboot

# Expose port (Railway will set PORT environment variable)
EXPOSE $PORT

# Health check using curl (more reliable than wget)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

# Run the application with production profile
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=production", "/app/app.jar"] 