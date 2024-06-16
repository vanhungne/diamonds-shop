# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Thiết lập mã hóa UTF-8
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Thiết lập mã hóa UTF-8
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

COPY --from=build /app/target/DrComputer-0.0.1-SNAPSHOT.war drcomputer.war
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "drcomputer.war"]
