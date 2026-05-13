# TripMate MVP

여행 동행을 구하는 모바일 우선 웹/웹뷰 앱 프로토타입입니다.

## 기술 스택

### Backend
- Java 17
- Spring Boot 3
- Spring Security
- JWT
- Spring Data JPA
- PostgreSQL

### Frontend
- Vite
- React
- React Router
- Axios

## 주요 기능

- 회원가입 / 로그인
- 내 프로필 조회/수정
- 동행 모집글 목록 조회
- 동행 모집글 상세 조회
- 동행 모집글 작성
- 동행 신청
- 내가 신청한 내역 조회
- 내 글에 들어온 신청 조회
- 신청 수락/거절
- 신고
- 사용자 차단

## 실행 방법

### 1. PostgreSQL 실행

```bash
docker compose up -d
```

### 2. Backend 실행

```bash
cd backend
./gradlew bootRun
```

Windows에서는:

```powershell
cd backend
gradlew.bat bootRun
```

기본 API 주소:

```text
http://localhost:8080
```

### 3. Frontend 실행

```bash
cd frontend
npm install
npm run dev
```

기본 프론트 주소:

```text
http://localhost:5173
```

## 테스트 계정 만들기

프론트에서 회원가입하거나 아래 API를 호출하세요.

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "test@test.com",
  "password": "1234",
  "nickname": "성주",
  "ageRange": "30대",
  "gender": "MALE"
}
```

## 다음에 개선하면 좋은 것

- Refresh Token
- 카카오/구글/애플 로그인
- 이미지 업로드(S3)
- 실시간 채팅
- 푸시 알림
- 후기/매너 점수
- 휴대폰 본인인증
- 관리자 신고 처리 화면
- 페이지네이션 고도화
- React Query 도입
- Capacitor로 앱 패키징
