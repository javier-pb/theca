# Dockerfile para Theca Backend
# Usa una imagen base de Eclipse Temurin con Java 21:
FROM eclipse-temurin:21-jdk-alpine AS build

# Establece el directorio de trabajo:
WORKDIR /app

# Copia el archivo pom.xml y descarga las dependencias:
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el código fuente y compila la aplicación:
COPY src ./src
RUN mvn clean package -DskipTests

# Imagen final para ejecutar la aplicación:
FROM eclipse-temurin:21-jre-alpine

# Copia el JAR generado desde la fase de build:
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto de la aplicación:
EXPOSE 8080

# Comando para ejecutar la aplicación:
ENTRYPOINT ["java", "-jar", "/app.jar"]