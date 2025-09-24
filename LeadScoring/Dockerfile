# Stage 1: Build the application
FROM maven:3.9.1-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build the JAR (skip tests for faster builds)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/LeadScoring-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Environment variables
ENV PORT=8080
ENV OPENAI_API_KEY="${OPENAI_API_KEY}"
ENV OPENAI_API_URL="${OPENAI_API_URL}"

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]

