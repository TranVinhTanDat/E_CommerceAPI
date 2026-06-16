# ----- Build stage: Maven + JDK 17 (Eclipse Temurin) -----
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy cấu hình Maven trước để tận dụng cache layer khi tải dependency
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline || true

# Copy mã nguồn và build JAR
COPY src ./src
RUN mvn -q clean package -DskipTests

# ----- Runtime stage: JRE 17 gọn nhẹ -----
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy JAR từ stage build
COPY --from=builder /app/target/shoppecommerce-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Spring Boot tự đọc biến môi trường SPRING_PROFILES_ACTIVE (mặc định 'prod' trên Render)
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
