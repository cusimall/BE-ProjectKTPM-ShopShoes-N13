FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Sao chép Maven wrapper và POM
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Đảm bảo mvnw có quyền thực thi
RUN chmod +x ./mvnw

# Tải dependencies
RUN ./mvnw dependency:go-offline -B

# Sao chép source code
COPY src src

# Build ứng dụng
RUN ./mvnw package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency

# Sao chép các thành phần cần thiết từ build stage
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Thiết lập biến môi trường
ENV SPRING_PROFILES_ACTIVE=docker

# Port mà ứng dụng sẽ lắng nghe
EXPOSE 8084

# Command để chạy ứng dụng
ENTRYPOINT ["java","-cp","app:app/lib/*","com.microservice.cartservice.CartServiceApplication"] 