# STAGE 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copiar archivos de configuración Maven
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Descargar dependencias
RUN ./mvnw dependency:go-offline -B

COPY src ./src

# Construir aplicación con capas habilitadas
RUN ./mvnw clean package -DskipTests

# Extraer capas del JAR
RUN java -Djarmode=tools -jar target/*.jar extract --layers --destination target/extracted

# STAGE 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario no-root para seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar capas
COPY --from=builder /app/target/extracted/dependencies/ ./
COPY --from=builder /app/target/extracted/spring-boot-loader/ ./
COPY --from=builder /app/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/target/extracted/application/ ./

# Cambiar ownership al usuario no-root
RUN chown -R appuser:appgroup /app

# Cambiar a usuario no-root
USER appuser

# Exponer puerto
EXPOSE 8081

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Punto de entrada usando JarLauncher extraído
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]