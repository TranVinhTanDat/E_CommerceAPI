# Sử dụng OpenJDK 17 làm môi trường build và chạy
FROM openjdk:17-jdk-slim AS builder

# Đặt thư mục làm việc
WORKDIR /app

# Copy file cấu hình Maven và mã nguồn
COPY pom.xml .
COPY src ./src

# Build project để tạo file JAR
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests

# Sử dụng image nhẹ hơn để chạy ứng dụng
FROM openjdk:17-jdk-slim

# Đặt thư mục làm việc trong container
WORKDIR /app

# Copy file JAR từ image builder
COPY --from=builder /app/target/shoppecommerce-0.0.1-SNAPSHOT.jar app.jar

# Mở cổng 8080
EXPOSE 8080

# Lệnh chạy ứng dụng với profile từ biến môi trường
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}", "/app/app.jar"]