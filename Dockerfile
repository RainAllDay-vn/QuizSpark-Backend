# Stage 1: Build the Spring Boot application
FROM gradle:8-jdk17-alpine AS build

WORKDIR /app

# Copy Gradle files first for better caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies (cached if build files unchanged)
RUN gradle dependencies --no-daemon || true

# Copy source code and build
COPY src ./src
COPY backend-private-key.json ./
RUN gradle bootJar --no-daemon

# Stage 2: Run the application
FROM amazoncorretto:17-alpine

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/build/libs/*.jar app.jar
COPY --from=build /app/backend-private-key.json ./

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=test

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]