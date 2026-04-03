FROM gradle:8.5-jdk17 AS build

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle build --no-daemon -x test

FROM openjdk:17-jdk-slim

EXPOSE 8080

COPY --from=build /home/gradle/src/build/libs/*-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Dserver.port=${PORT:8080}", "-jar", "/app.jar"]
