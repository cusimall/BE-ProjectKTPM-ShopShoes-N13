#!/bin/bash
# Build all Docker images
echo "Building all Docker images..."

# Build Discovery Server
echo "Building Discovery Server..."
cd discovery-server
./mvnw clean package -DskipTests
cd ..

# Build API Gateway
echo "Building API Gateway..."
cd api-gateway
./mvnw clean package -DskipTests
cd ..

# Build Authentication Service
echo "Building Authentication Service..."
cd authentication-service
./mvnw clean package -DskipTests
cd ..

# Build Product Service
echo "Building Product Service..."
cd product-service
./mvnw clean package -DskipTests
cd ..

# Build Cart Service
echo "Building Cart Service..."
cd cart-service
./mvnw clean package -DskipTests
cd ..

# Build Invoice Service
echo "Building Invoice Service..."
cd invoice-service
./mvnw clean package -DskipTests
cd ..

# Run Docker Compose
echo "Running Docker Compose..."
docker-compose up -d 