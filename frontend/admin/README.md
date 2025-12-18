# Auction Admin Panel

관리자용 백오피스 웹 페이지

## 📁 파일 구조

```
frontend/admin/
├── admin-login.html          # 관리자 로그인
├── dashboard.html            # 대시보드 (통계 및 차트)
├── auction-request.html      # 경매 승인/거절
├── users.html                # 회원 관리 (차단/해제)
└── js/
    └── admin-api.js          # 공통 API 모듈
```

## 🚀 실행 방법

### 1. Live Server 실행

```bash
cd frontend/admin
npx serve .
```

### 2. 브라우저 접속

```
http://127.0.0.1:5500/admin-login.html
```

### 3. 관리자 로그인

- **이메일**: 관리자 계정 이메일
- **비밀번호**: 관리자 비밀번호

**⚠️ 중요**: `ROLE_ADMIN` 권한이 있는 계정만 접근 가능합니다.

## 📊 주요 기능

### 1️⃣ 대시보드 (`dashboard.html`)

- **실시간 통계 카드**
  - 전체 경매 수
  - 진행 중인 경매
  - 승인 대기 경매
  - 전체 회원 수


- **Elasticsearch 기반 차트**
  - 카테고리별 경매 분포 (Bar Chart)
  - 상태별 분포 (Doughnut Chart)
  - 시간대별 등록 추이 (Line Chart)


**API**: `GET /api/v1/admin/dashboard/stats`

### 2️⃣ 경매 승인 (`auction-request.html`)

- **승인 대기 목록 조회**
  - 상태가 `PENDING`인 경매만 표시
  - 페이지네이션 지원

- **관리 기능**
  - ✅ 승인: `POST /api/v1/admin/auctions/{id}/approve`
  - ❌ 거절: `POST /api/v1/admin/auctions/{id}/reject`

### 3️⃣ 회원 관리 (`users.html`)

- **회원 목록 조회**
  - 상태 필터링 (전체/활성/차단/비활성/탈퇴)
  - 페이지네이션 지원

- **관리 기능**
  - 🚫 차단: `POST /api/v1/admin/users/{id}/ban`
  - 🔓 해제: `POST /api/v1/admin/users/{id}/unban`

## 🔒 보안 기능

### JWT 토큰 기반 인증

```javascript
// 로그인 시
setAdminToken(accessToken);  // localStorage 저장

// 모든 API 요청 시
Authorization: "Bearer {token}"
```

### 권한 체크

```javascript
function checkAdminRole() {
    const admin = getCurrentAdmin();
    
    if (!admin.roles.includes('ROLE_ADMIN')) {
        alert('관리자 권한이 없습니다.');
        window.location.href = 'admin-login.html';
        return false;
    }
    
    return true;
}
```

**모든 관리 페이지에서 실행:**
- `dashboard.html`
- `auction-request.html`
- `users.html`

### 자동 리다이렉트

```javascript
// 401 Unauthorized → 로그인 페이지
// 403 Forbidden → 권한 없음 알림
```

## 📝 API 엔드포인트

### 인증
- `POST /api/v1/auth/login` - 로그인

### 대시보드
- `GET /api/v1/admin/dashboard/stats` - 통계 조회

### 회원 관리
- `GET /api/v1/admin/users` - 회원 목록
- `GET /api/v1/admin/users/{id}` - 회원 상세
- `POST /api/v1/admin/users/{id}/ban` - 회원 차단
- `POST /api/v1/admin/users/{id}/unban` - 차단 해제

### 경매 관리
- `GET /api/v1/admin/auctions` - 경매 목록
- `GET /api/v1/admin/auctions/pending` - 승인 대기 목록
- `GET /api/v1/admin/auctions/{id}` - 경매 상세
- `POST /api/v1/admin/auctions/{id}/approve` - 승인
- `POST /api/v1/admin/auctions/{id}/reject` - 거절
- `DELETE /api/v1/admin/auctions/{id}` - 삭제

## ⚙️ 설정

### Gateway URL 변경

`frontend/admin/js/admin-api.js`:

```javascript
const GATEWAY_URL = 'http://localhost:8000';
```

