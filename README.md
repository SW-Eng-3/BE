# SW-Eng-3 Backend (BE)

이 프로젝트는 SW 공학 3팀의 백엔드 시스템입니다. Spring Boot를 기반으로 하며, 커뮤니티, 멘토링, 게임화(포인트) 등의 기능을 제공합니다.

## 🚀 기술 스택

- **Framework:** Spring Boot 3.x
- **Language:** Java 17
- **Database:** PostgreSQL (JPA/Hibernate)
- **Caching & Session:** Redis
- **Security:** Spring Security, JWT
- **Documentation:** Swagger (Springdoc OpenAPI)
- **Build Tool:** Gradle

## 📂 프로젝트 구조

```text
src/main/java/yc/sw3/backend/
├── config/           # 설정 (Database, Security, Web)
├── controller/       # API 컨트롤러
├── domain/           # 엔티티 및 리포지토리 (Domain Driven Design)
├── dto/              # 데이터 전송 객체
├── service/          # 비즈니스 로직
└── BackendApplication.java
```

## 🛠 주요 기능

- **Auth:** JWT 기반 회원가입 및 로그인, 이메일 인증
- **Community:** 게시글 및 댓글 관리
- **Mentoring:** 멘토 일정 관리 및 멘토링 요청/승인
- **Gamification:** 활동에 따른 포인트 적립 및 이력 관리
- **Report:** 부적절한 콘텐츠 신고 시스템

## ⚙️ 설정 방법

1. 리포지토리를 클론합니다.
2. `src/main/resources/application.yaml` 파일을 환경에 맞게 설정합니다.
3. `./gradlew bootRun` 명령어로 애플리케이션을 실행합니다.

## 📄 라이선스

이 프로젝트는 팀 내부용으로 제작되었습니다.
