# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jdk-slim as build

ARG SBT_VERSION=1.10.1

# Set the working directory inside the container
WORKDIR /app

# Install sbt
RUN apt update && \
    apt install -y curl && \
    curl -L -o sbt-$SBT_VERSION.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-$SBT_VERSION.deb && \
    dpkg -i sbt-$SBT_VERSION.deb && \
    rm sbt-$SBT_VERSION.deb && \
    apt install -y sbt && \
    apt clean

# Copy the build.sbt and project/ directory
COPY build.sbt .
COPY project/ project/

# Download dependencies
RUN sbt update

# Copy the rest of the application code
COPY src/ src/

# Build the application
RUN sbt assembly

# Use a smaller base image for the final stage
FROM openjdk:11-jre-slim

# Set the working directory
WORKDIR /app

# Copy the built artifact from the build stage
COPY --from=build /app/target/scala-3.3.3/chai-assembly-1.0.0.jar /app/http-app.jar

# Expose the port your app runs on
EXPOSE 9000

# Command to run the application
ENTRYPOINT ["java", "-jar", "http-app.jar"]