# Docker Hub 설정 가이드

## 📋 개요

CI/CD 파이프라인에서 Docker Hub에 이미지를 자동으로 Push하기 위한 설정 가이드.

---

## 🔐 1. Docker Hub 액세스 토큰 생성

### 1단계: Docker Hub 로그인

1. [Docker Hub](https://hub.docker.com/) 접속
2. 계정으로 로그인

### 2단계: 액세스 토큰 생성

1. 우측 상단 프로필 아이콘 클릭
2. **Account Settings** 선택
3. 좌측 메뉴에서 **Security** 클릭
4. **New Access Token** 버튼 클릭

### 3단계: 토큰 설정

```
Token Description: github-actions-ci
Access permissions: Read, Write, Delete
```

1. **Generate** 버튼 클릭
2. 생성된 토큰 복

---

## 🔗 2. GitHub Repository Secrets 설정

### 1단계: Repository Settings 접속

1. GitHub Repository 페이지 이동
2. 상단 메뉴에서 **Settings** 클릭
3. 좌측 메뉴에서 **Secrets and variables** → **Actions** 클릭

### 2단계: Secret 추가

#### `DOCKER_USERNAME` 추가

1. **New repository secret** 버튼 클릭
2. Name: `DOCKER_USERNAME`
3. Secret: Docker Hub 사용자명 입력
4. **Add secret** 클릭

#### `DOCKER_PASSWORD` 추가

1. **New repository secret** 버튼 클릭
2. Name: `DOCKER_PASSWORD`
3. Secret: 복사한 Docker Hub 액세스 토큰 붙여넣기
4. **Add secret**

### 최종 확인

Secrets 목록에 다음 2개가 표시되어야 합니다:

```
✅ DOCKER_USERNAME
✅ DOCKER_PASSWORD
```

---

## 📦 3. Docker Hub Repository 생성 (선택 사항)

### 자동 생성

- CI/CD 파이프라인이 처음 실행될 때 자동으로 생성됨
- Public Repository로 생성됨

### 수동 생성 (Private Repository를 원할 경우)

1. Docker Hub 로그인
2. 상단 메뉴에서 **Repositories** 클릭
3. **Create Repository** 클릭
4. 정보 입력:
```
Repository Name: auction-service-user
Visibility: Private
```
5. **Create**

**생성할 Repository 목록:**
- `auction-server-discovery`
- `auction-server-gateway`
- `auction-service-user`
- `auction-service-auction`
- `auction-service-payment`
- `auction-service-search`
- `auction-service-chat`
- `auction-service-admin`
- `auction-service-product`
- `auction-frontend`

---

## 🧪 4. 로컬에서 테스트

### Docker Hub 로그인 테스트

```bash
# Docker CLI로 로그인
docker login

# Username 입력
Username: {username}

# Password 입력 (액세스 토큰)
Password: {access_token}

Login Succeeded
```

### Jib 빌드 테스트

```bash
# 환경 변수 설정
export DOCKER_USERNAME={username}
export DOCKER_PASSWORD={access_token}

# 로컬 Docker Daemon에 빌드 (Push 없음)
./gradlew :service-user:jibDockerBuild

# Docker 이미지 확인
docker images | grep auction

```

### Docker Hub에 Push 테스트

```bash
# Docker Hub에 직접 Push
./gradlew :service-user:jib

# 성공 메시지 확인
# Built and pushed image as {username}/auction-service-user:latest
```

---

## 🚀 5. GitHub Actions 워크플로우 테스트

### 1단계: 코드 Push

```bash
git add .
git commit -m "CI/CD 파이프라인 설정"
git push origin main
```

### 2단계: 워크플로우 확인

1. GitHub Repository → **Actions** 탭
2. 최신 워크플로우 실행 확인
3. **test-and-build** Job 클릭하여 로그 확인

### 3단계: Docker Hub 확인

1. [Docker Hub](https://hub.docker.com/) 로그인
2. **Repositories** 클릭
3. `auction-service-user` Repository 확인
4. **Tags** 탭에서 `latest`, `0.0.1-SNAPSHOT-xxxxx` 태그 확인

---
