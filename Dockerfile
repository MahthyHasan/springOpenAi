# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy the project files
COPY Backend/Hasan/pom.xml .
COPY Backend/Hasan/src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "app.jar"]