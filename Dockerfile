# Multi-stage build for Spring PetClinic
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# Copy the entire project
COPY . .

# Build the application using Maven
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD java -jar /app/jars/libjdwp.so || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]