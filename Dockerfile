FROM maven:3.9-eclipse-temurin-23 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:23-jre-alpine AS runtime
WORKDIR /app
COPY --from=build /build/target/linkly-shortener-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]


