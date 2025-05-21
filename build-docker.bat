@echo off
REM Build all Docker images
echo Building all Docker images...

REM Build Discovery Server
echo Building Discovery Server...
cd discovery-server
call mvnw clean package -DskipTests
cd ..

REM Build API Gateway
echo Building API Gateway...
cd api-gateway
call mvnw clean package -DskipTests
cd ..

REM Build Authentication Service
echo Building Authentication Service...
cd authentication-service
call mvnw clean package -DskipTests
cd ..

REM Build Product Service
echo Building Product Service...
cd product-service
call mvnw clean package -DskipTests
cd ..

REM Build Cart Service
echo Building Cart Service...
cd cart-service
call mvnw clean package -DskipTests
cd ..

REM Build Invoice Service
echo Building Invoice Service...
cd invoice-service
call mvnw clean package -DskipTests
cd ..

REM Run Docker Compose
echo Running Docker Compose...
docker-compose up -d 