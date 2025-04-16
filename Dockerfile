# Sử dụng image OpenJDK 17 làm base image
FROM openjdk:17-jdk-slim

# Cài đặt Maven (nếu dùng Maven)
RUN apt-get update && apt-get install -y maven

# Đặt thư mục làm việc
WORKDIR /app

# Copy toàn bộ mã nguồn vào container
COPY . .

# Build ứng dụng để tạo file JAR
RUN mvn clean package -DskipTests

# Copy file JAR đã build vào tên app.jar
COPY target/shoppecommerce-0.0.1-SNAPSHOT.jar app.jar

# Mở cổng 8080
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]