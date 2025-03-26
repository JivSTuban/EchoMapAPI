# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Add wait-for-it script to wait for database
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# Install dependencies
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    ca-certificates \
    netcat-openbsd && \
    rm -rf /var/lib/apt/lists/*

# Create a directory for persistent data
RUN mkdir -p /data
VOLUME /data

# The PORT environment variable is set by Fly.io
ENV PORT=8080
EXPOSE 8080

# Set the default Java options
ENV JAVA_OPTS=""

# Start the application
CMD java $JAVA_OPTS -jar app.jar
