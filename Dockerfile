# Stage 1: Build
FROM maven:3.9.1-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy built jar from previous stage
COPY --from=build /app/target/*.jar app.jar

# Pass Render's dynamic PORT to Spring Boot
ENV PORT=10000
ENV JAVA_OPTS=""

# Entrypoint with dynamic port binding
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar"]
