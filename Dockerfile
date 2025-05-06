# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built application JAR file into the container
COPY target/*.jar app.jar

# Set the Spring profile to 'prod' when the application starts
ENV SPRING_PROFILES_ACTIVE=prod

# Expose the port your application will run on (default is 8080 for Spring Boot)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
