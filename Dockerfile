# Sử dụng JDK 17 làm nền tảng
FROM openjdk:17-jdk-alpine

# Đặt thư mục làm việc trong container
WORKDIR /app

# Sao chép file JAR của ứng dụng vào container
COPY target/shoppecommerce-0.0.1-SNAPSHOT.jar app.jar

# Khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
