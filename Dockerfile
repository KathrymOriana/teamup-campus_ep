# ---------- Etapa 1: compilar con Maven ----------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---------- Etapa 2: imagen final ligera, sin Maven ni código fuente ----------
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 1001 spring && chown -R spring /app
COPY --from=build /app/target/*.jar app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
