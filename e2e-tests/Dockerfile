# Use the official Gradle image with JDK 21 and Gradle 8.11.1
FROM gradle:8.11.1-jdk21

# Install Docker CLI to be able to run Localstack tests
RUN apt-get update && apt-get install -y docker.io

# Copy the Gradle wrapper and build files from the root to the working directory
COPY ../gradlew .
COPY ../gradle gradle
COPY ../settings.gradle .

# Copy the specific e2e-tests module
COPY e2e-tests ./e2e-tests

# Ensure Gradle wrapper is executable
RUN chmod +x ./gradlew

# Run the tests for the e2e-tests module
CMD ["./gradlew", ":e2e-tests:test"]