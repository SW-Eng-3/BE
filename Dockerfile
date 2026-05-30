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
# - Dserver.port=${PORT:-10000}: Render의 PORT 환경 변수 사용, 기본값 10000
# - Dserver.address=0.0.0.0: 모든 인터페이스에서 수신
# - Xmx256m -Xms256m: 512MB 환경에서 힙 메모리를 256MB로 제한하여 Metaspace 등 여유 공간 확보
# - XX:+UseParallelGC: 메모리가 적은 환경에서 효율적인 GC 사용
ENTRYPOINT ["sh", "-c", "java \
            -Dspring.profiles.active=prod \
            -Dserver.port=${PORT:-10000} \
            -Dserver.address=0.0.0.0 \
            -Xmx256m \
            -Xms256m \
            -XX:+UseParallelGC \
            -jar app.jar"]
