# Sử dụng image OpenJDK 17 làm base image
FROM openjdk:17-jdk-slim

# Cài đặt Maven
RUN apt-get update && apt-get install -y maven

# Đặt thư mục làm việc
WORKDIR /app

# Copy file cấu hình và mã nguồn
COPY pom.xml .
COPY src ./src

# Copy file JWT key
COPY src/main/resources/private_key.pem /app/private_key.pem
COPY src/main/resources/public_key.pem /app/public_key.pem

# Build ứng dụng
RUN mvn clean package -DskipTests

# Debug: kiểm tra xem file JAR có tồn tại không
RUN ls -la target/

# Copy file JAR
COPY target/shoppecommerce-0.0.1-SNAPSHOT.jar app.jar

# Mở cổng 8080
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]