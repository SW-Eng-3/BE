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

## 💬 WebSocket 채팅 사용법

선배와 1:1 실시간 채팅은 WebSocket 기반으로 동작합니다. 메시지는 DB의 `chat_messages` 테이블에 저장되고, 채팅방의 최근 메시지는 `chat_rooms` 테이블에 갱신됩니다.

### 1. 사전 준비

1. 로그인 또는 테스트 토큰 발급을 통해 JWT access token을 준비합니다.
2. 선배와의 채팅방을 생성하거나 기존 채팅방을 조회합니다.

```http
POST /api/v1/chats/rooms/senior
Authorization: Bearer {JWT_ACCESS_TOKEN}
Content-Type: application/json

{
  "seniorId": "{SENIOR_USER_ID}"
}
```

응답 예시:

```json
{
  "roomId": "{CHAT_ROOM_ID}",
  "studentId": "{STUDENT_USER_ID}",
  "studentName": "학생",
  "seniorId": "{SENIOR_USER_ID}",
  "seniorName": "선배",
  "status": "ACTIVE",
  "lastMessage": null,
  "lastMessageAt": null,
  "messages": []
}
```

### 2. WebSocket 연결

로컬 실행 시 WebSocket 주소는 다음과 같습니다.

```text
ws://localhost:8080/ws/chat?roomId={CHAT_ROOM_ID}&token={JWT_ACCESS_TOKEN}
```

HTTPS 운영 환경에서는 `wss://`를 사용합니다.

```text
wss://{BACKEND_DOMAIN}/ws/chat?roomId={CHAT_ROOM_ID}&token={JWT_ACCESS_TOKEN}
```

### 3. 메시지 전송

WebSocket 연결 후 아래 JSON을 전송합니다.

```json
{
  "roomId": "{CHAT_ROOM_ID}",
  "content": "안녕하세요"
}
```

### 4. 메시지 수신

같은 채팅방에 접속 중인 사용자에게 아래 형식으로 메시지가 전달됩니다.

```json
{
  "messageId": "{MESSAGE_ID}",
  "roomId": "{CHAT_ROOM_ID}",
  "senderId": "{SENDER_USER_ID}",
  "senderName": "학생",
  "content": "안녕하세요",
  "messageType": "CHAT",
  "createdAt": "2026-05-24T15:30:00"
}
```

오류가 발생하면 다음 형식의 메시지가 전달될 수 있습니다.

```json
{
  "type": "ERROR",
  "message": "Message content is required"
}
```

### 5. 관련 REST API

- `POST /api/v1/chats/rooms/senior` : 선배와의 채팅방 생성 또는 조회
- `GET /api/v1/chats/rooms/senior?seniorId={seniorId}` : 쿼리 방식 채팅방 생성 또는 조회
- `GET /api/v1/chats/rooms` : 내 채팅방 목록 조회
- `GET /api/v1/chats/rooms/{roomId}` : 채팅방 상세 및 메시지 조회
- `GET /api/v1/chats/rooms/{roomId}/messages` : 채팅방 메시지 목록 조회
- `POST /api/v1/chats/rooms/{roomId}/messages` : WebSocket 없이 메시지 저장

### 6. 간단 테스트 흐름

1. `./gradlew bootRun`으로 서버를 실행합니다.
2. 브라우저에서 `http://localhost:8080/index.html`에 접속합니다.
3. 학생 계정과 졸업생/교수 계정을 생성하고 각각 토큰을 발급합니다.
4. 멘토링 탭에서 `1:1 채팅` 버튼을 클릭합니다.
5. 채팅 화면에서 메시지를 전송합니다.
6. 다른 브라우저 창에서 상대 계정으로 접속해 실시간 수신을 확인합니다.
7. 새로고침 후 기존 메시지가 다시 조회되는지 확인합니다.

## 📄 라이선스

이 프로젝트는 팀 내부용으로 제작되었습니다.
