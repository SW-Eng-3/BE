FROM gradle:8.5-jdk17 AS build
COPY . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -x test -Dorg.gradle.jvmargs="-Xmx512m"

FROM openjdk:17-jdk-slim
WORKDIR /app
EXPOSE 8080

COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Dserver.port=${PORT:8080}", "-jar", "app.jar"]
