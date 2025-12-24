# Build stage: Gradle로 빌드
FROM gradle:8-jdk17 AS build
WORKDIR /app

# 프로젝트 파일 복사
COPY . .

# ShadowJar 빌드 (의존성 포함 Fat JAR 생성)
RUN ./gradlew :server:shadowJar --no-daemon

# Runtime stage: JRE만 사용
# alpine 태그가 ARM64를 지원하지 않을 수 있으므로 일반 이미지 사용
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드된 Fat JAR 복사 (shadowJar는 기본적으로 -all suffix를 붙임)
COPY --from=build /app/server/build/libs/server-all.jar app.jar

# 환경변수 (필수: docker run 시 주입 필요)
# 사용 예시:
#   docker run -e SUPABASE_URL="https://your-project.supabase.co" \
#              -e SUPABASE_SERVICE_ROLE_KEY="your_service_role_key" \
#              -p 8080:8080 \
#              parkingpass-server:latest
ENV SUPABASE_URL=""
ENV SUPABASE_SERVICE_ROLE_KEY=""

# 포트 노출
EXPOSE 8080

# 서버 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

