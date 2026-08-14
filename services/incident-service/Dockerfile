# ============================================================
# STAGE 1: BUILD THE APPLICATION
# ============================================================
# Use a Docker image that already contains:
#   - Maven 3.9        -> builds our Java application
#   - Java 17          -> needed to compile the application
#   - Alpine Linux     -> lightweight Linux distribution
#
# "AS builder" gives this build stage the name "builder".
# We will use this name later to copy the JAR into Stage 2.
FROM maven:3.9-eclipse-temurin-17-alpine AS builder


# Set /app as the working directory inside the container.
# Similar to running:
#
#     cd /app
#
# All commands below will work from /app.
WORKDIR /app


# Copy pom.xml from our computer into /app inside the container.
#
# pom.xml contains information Maven needs to build our project,
# such as:
#   - project information
#   - dependencies
#   - Java version
#   - Maven plugins
COPY pom.xml .


# Copy our entire src directory from our computer into /app/src
# inside the container.
#
# At this point the builder container looks roughly like:
#
# /app
# ├── pom.xml
# └── src
#     ├── main
#     └── test
COPY src ./src


# Tell Maven to build the application.
#
# "clean"       -> removes previous build files
# "package"     -> compiles the Java code and creates the JAR
# "-DskipTests" -> builds the application without running tests
#
# After this command, Maven will create something like:
#
# /app/target/
# └── incident-service-0.0.1-SNAPSHOT.jar
#
# This JAR is the finished, compiled Spring Boot application.
RUN mvn clean package -DskipTests


# ============================================================
# STAGE 2: CREATE THE RUNTIME IMAGE
# ============================================================
# Start a NEW image for actually running the application.
#
# This image only contains Java 17 JRE and Alpine Linux.
#
# We don't need Maven here because the application has already
# been compiled into a JAR in Stage 1.
#
# This makes the final image:
#   - smaller
#   - cleaner
#   - more secure
#
# The Maven image from Stage 1 is NOT included in the final image.
FROM eclipse-temurin:17-jre-alpine


# Set /app as the working directory inside the runtime container.
#
# So our application will live inside:
#
# /app/incident-manager.jar
WORKDIR /app


# Copy the JAR created in Stage 1 into this runtime image.
#
# "--from=builder" means:
#   "Get the file from the Docker build stage named 'builder'."
#
# /app/target/*.jar
#       ↑
# JAR created by Maven in Stage 1
#
# /app/incident-manager.jar
#       ↑
# Where we put it in the final runtime image
#
# We also rename the JAR to "incident-manager.jar".
COPY --from=builder /app/target/*.jar /app/incident-manager.jar


# Document that the application is expected to listen on port 6567.
#
# IMPORTANT:
# EXPOSE does not actually publish the port to your computer.
#
# When running the container, you would normally use:
#
# docker run -p 6567:6567 incident-manager
#
# This maps:
#
# your computer:6567
#        ↓
# container:6567
EXPOSE 6567


# Command that starts the application whenever the container starts.
#
# This is equivalent to running:
#
# java -jar incident-manager.jar
#
# Because WORKDIR is /app, Docker will run:
#
# java -jar /app/incident-manager.jar
#
# This is the actual command that starts your Spring Boot
# incident-management application.
ENTRYPOINT ["java", "-jar", "incident-manager.jar"]