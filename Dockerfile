# ── Etapa 1: Build ────────────────────────────────────────────────────────────
# Usamos amazoncorretto:21-alpine como base — es la distribución de OpenJDK
# mantenida por Amazon, optimizada para correr en AWS
# La variante alpine minimiza el tamaño de la imagen
FROM amazoncorretto:21-alpine AS builder

WORKDIR /app

# Copiamos primero los archivos de configuración de Gradle
# Docker cachea esta capa si los archivos no cambian — evita
# descargar dependencias en cada build si solo cambió el código fuente
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY checkstyle.xml .

# Damos permisos de ejecución al wrapper de Gradle
RUN chmod +x gradlew

# Descargamos dependencias — esta capa queda cacheada si build.gradle no cambia
RUN ./gradlew dependencies --no-daemon

# Ahora copiamos el código fuente
COPY src src

# Construimos el JAR excluyendo tests y análisis estático
# Los tests ya corrieron en el pipeline CI — no los repetimos aquí
RUN ./gradlew bootJar --no-daemon -x test -x checkstyleMain -x checkstyleTest

# ── Etapa 2: Runtime ──────────────────────────────────────────────────────────
# La imagen final solo contiene el JRE y el JAR — sin JDK ni Gradle
# Pesa ~120 MB vs ~500 MB de la etapa builder
FROM amazoncorretto:21-alpine

WORKDIR /app

# Creamos un usuario no root para ejecutar la aplicación
# Si un atacante ejecuta código dentro del contenedor,
# un usuario sin privilegios limita significativamente el daño potencial
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiamos SOLO el JAR desde la etapa builder — no el código fuente ni Gradle
COPY --from=builder /app/build/libs/*.jar app.jar

# Cambiamos el propietario del JAR al usuario no root
RUN chown appuser:appgroup app.jar

# Cambiamos al usuario no root antes de arrancar
USER appuser

# Documentamos el puerto que expone el contenedor
EXPOSE 8085

# Opciones de JVM — límites de memoria conservadores para t2.micro
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Comando de arranque — usa la variable JAVA_OPTS definida arriba
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]