# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src/ /app/src/
RUN mvn package -DskipTests

# Run stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Install curl and netcat for connection checking
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl ca-certificates netcat-openbsd && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create a script for database connection check
RUN echo '#!/bin/bash \n\
host=$(echo $JDBC_DATABASE_URL | sed -E "s/jdbc:postgresql:\/\/([^:]+).*/\1/") \n\
port=5432 \n\
echo "Waiting for database connection at $host:$port..." \n\
while ! nc -z $host $port; do \n\
  echo "Database not available yet..." \n\
  sleep 1 \n\
done \n\
echo "Database is available, starting application..." \n\
exec java $JAVA_OPTS -jar app.jar \n\
' > /app/start.sh && chmod +x /app/start.sh

# Create data directory for persistence
RUN mkdir -p /data
VOLUME /data

# The PORT environment variable is set by Fly.io
ENV PORT=8080
EXPOSE 8080

# Set the default Java options
ENV JAVA_OPTS="-XX:+UseContainerSupport -Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"

# Start the application
CMD ["/app/start.sh"]
