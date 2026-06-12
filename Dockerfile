# Stage 1 — build frontend (no VITE_API_URL needed: same origin as backend)
FROM node:20-alpine AS frontend
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install
COPY frontend/ .
RUN npm run build

# Stage 2 — build backend + embed frontend static files
FROM maven:3.9.6-eclipse-temurin-17 AS backend
WORKDIR /app
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B -q
COPY backend/src ./src
# Inject built frontend so Spring Boot serves it from /
COPY --from=frontend /frontend/dist ./src/main/resources/static
RUN mvn package -DskipTests -B -q

# Stage 3 — minimal runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend /app/target/*.jar app.jar
EXPOSE 8080
# Limit heap to 75% of container memory — fits Fly.io 512MB VMs
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
