# =============================================================================
# DOCKERFILE - Multi-stage Build for java-foundation-lab
# Arquitectura: Build con Maven + Runtime ligero
# =============================================================================
# ETAPA 1: COMPILACIÓN (Build)
# ----------------------------------------------------------------------------
# Imagen base: Maven oficial con Java 24
# - Cache de dependencias capas para builds rápidos
# - Ejecuta mvn clean package -DskipTests
# ----------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin24 AS build

# Establecer directorio de trabajo
WORKDIR /app

# Optimización de capas: copiar primero archivos de configuración Maven
# para aprovechar el caché de Docker entre builds
COPY pom.xml .

# Descargar dependencias SOLAMENTE (sin código fuente)
# Esto crea una capa separada que se reutiliza si solo cambian fuentes
RUN mvn dependency:go-offline -B

# Copiar código fuente del proyecto
COPY src ./src

# ETAPA DE COMPILACIÓN
# ----------------------------------------------------------------------------
# Compilar el proyecto Spring Boot
# - Packages as JAR con nombre predictible
# - Omite tests para acelerar el build de la imagen (se ejecutan por separado)
# ----------------------------------------------------------------------------
RUN mvn clean package -DskipTests -J-Xmx512m

# Verificar que el JAR fue creado
RUN ls -la target/*.jar

# ETAPA 2: RUNTIME (Ejecución)
# ----------------------------------------------------------------------------
# Imagen base: JDK 24 ligero (slim) - o usar OpenJDK:24-slim si se necesita
# alternativa: eclipse-temurin:24-jre-slim para solo JRE (sin herramientas de desarrollo)
# ----------------------------------------------------------------------------
FROM eclipse-temurin:24-jre-alpine

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el JAR generado desde la etapa de compilación
# Se asume que el JAR tiene el nombre estándar de Spring Boot: java-foundation-lab-1.0.0-SNAPSHOT.jar
# Si el nombre es diferente, ajustar aquí
COPY --from=build /app/target/java-foundation-lab-1.0.0-SNAPSHOT.jar app.jar

# Expone el puerto en el que escucha Spring Boot
EXPOSE 8080

# Configurar variables de entorno de Java para optimización
# - XX:+UseContainerSupport: Optimizaciones de Kubernetes/Docker para memoria limitada
# - XX:MaxRAMPercentage: Limitar uso de memoria al 75% del container (buen practice)
# - Duser.timezone: Configurar zona horaria por defecto
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=0.75 -Duser.timezone=UTC"

# Comando de entrada por defecto
# - Ejecuta la aplicación Spring Boot JAR
# - $JAVA_OPTS se inyecta automáticamente
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Health check: Verificar que la aplicación está lista para recibir tráfico
# - Espera a que el endpoint /actuator/health esté disponible
# - Si falla, Docker reinicia el container o marca como unhealthy
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Etiquetas/metadata sugeridas para docker run / compose
# Labels para mejor descubrimiento y operación
LABEL \
    org.opencontainers.image.title="java-foundation-lab" \
    org.opencontainers.image.description="Java Foundation Lab - 11 Phases Integrated" \
    org.opencontainers.image.version="1.0.0-SNAPSHOT" \
    org.opencontainers.image.vendor="SimDev" \
    org.opencontainers.image.schema-version="1.0.1"

# Notas de operación:
# - La imagen usa eclipse-temurin:24-jre-alpine para tamaño reducido (~100MB vs ~350MB)
# - El healthcheck verifica el endpoint actuator de Spring Boot
# - Las variables JAVA_OPTS optimizan el uso de memoria en containers
# - Multi-stage build reduce la imagen final a ~200MB aprox
# - Para producción, considerar añadir configuración de logging a filebeat/fluentd