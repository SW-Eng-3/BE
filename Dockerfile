# 1단계: 빌드 환경 (Gradle 캐시 활용)
FROM gradle:8.5-jdk17 AS build
WORKDIR /home/gradle/src

# 종속성만 먼저 복사하여 캐시 활용
COPY build.gradle settings.gradle /home/gradle/src/
RUN gradle build --no-daemon -x test > /dev/null 2>&1 || true

# 소스 코드 복사 및 빌드
COPY . /home/gradle/src
RUN gradle build --no-daemon -x test -Dorg.gradle.jvmargs="-Xmx512m"

# 2단계: 실행 환경 (최소 이미지)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Render의 포트 환경 변수 대응
EXPOSE 10000

# 빌드 결과물만 복사
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

# Render(512MB 플랜) 최적화 JVM 옵션 적용
# -XX:MaxRAMPercentage: 컨테이너 메모리의 75%까지 JVM이 사용하도록 설정
ENTRYPOINT ["java", \
            "-Dspring.profiles.active=prod", \
            "-Dserver.port=${PORT:8080}", \
            "-XX:MaxRAMPercentage=75.0", \
            "-jar", "app.jar"]
