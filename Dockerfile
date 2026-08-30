FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

RUN JAR_FILE=$(find target -maxdepth 1 -type f \
    -name 'cloudops-*.jar' \
    ! -name '*-lambda.jar' \
    | head -n 1) \
    && cp "$JAR_FILE" target/app.jar

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]