# Stage 1: Build ứng dụng
FROM openjdk:17-jdk-slim AS build
RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY src/main/resources/private_key.pem /app/private_key.pem
COPY src/main/resources/public_key.pem /app/public_key.pem
RUN mvn clean package -DskipTests
# Debug: kiểm tra file JAR
RUN ls -la target/

# Stage 2: Tạo image chạy ứng dụng
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/shoppecommerce-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]