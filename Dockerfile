# ---- Build stage: compiles the app with Maven ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ---- Run stage: only the JRE + the built jar, much smaller than the build image ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

# -Xmx400m caps the heap so the JVM fits inside Render's free 512MB RAM limit —
# without this, Spring Boot's default heap sizing can be too greedy and the
# container gets killed under load.
ENTRYPOINT ["java", "-Xmx400m", "-jar", "app.jar"]
