# CI/CD 파이프라인 구현 완료 ✅

## 🎉 개요

GitHub Actions와 Jib를 사용한 **CI/CD 파이프라인** 구축

---

## ✅ 완료된 작업

### 1. ✅ GitHub Actions Workflow
**파일:** `.github/workflows/deploy.yml`

- **test-and-build:** 모든 브랜치에서 테스트 & 빌드 실행
- **docker-build-push:** `main` 브랜치에서 Docker 이미지 빌드 & Push
- **docker-build-frontend:** Frontend Nginx 이미지 빌드
- **notify:** 배포 결과 알림

### 2. ✅ Gradle Jib 플러그인 설정
**파일:** `build.gradle.kts`

```kotlin
plugins {
    id("com.google.cloud.tools.jib") version "3.4.0" apply false
}
```

## 🚀 워크플로우 동작 방식

### 📊 Flow Diagram

```
┌──────────────────────────────────────────────┐
│  코드를 main 브랜치에 Push
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│  GitHub Actions Trigger
│  - Event: push
│  - Branch: main
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│  Job 1: test-and-build
│  ✓ Checkout 코드
│  ✓ JDK 21 설정
│  ✓ ./gradlew clean test
│  ✓ ./gradlew build -x test
│  ✓ 테스트 결과 & 빌드 아티팩트 업로드         
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│  Job 2: docker-build-push (병렬)             
│  ┌────────────────────────────────────────┐
│  │  Matrix Strategy
│  │  - server-discovery
│  │  - server-gateway
│  │  - service-user
│  │  - service-auction
│  │  - service-payment
│  │  - service-search
│  │  - service-chat
│  │  - service-admin
│  │  - service-product
│  └────────────────────────────────────────┘
│
│  각 서비스:
│  ✓ Docker Hub 로그인
│  ✓ Jib 빌드
│  ✓ 이미지 Push (2개 태그)
│    - {version}-{sha}
│    - latest
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│  Job 3: docker-build-frontend
│  ✓ Docker Buildx 설정
│  ✓ Frontend 이미지 빌드
│  ✓ Docker Hub에 Push
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│  Job 4: notify
│  ✓ 모든 작업 성공/실패 확인
│  ✓ 결과 알림
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│  Docker Hub에 이미지 배포 완료
│  ✅ 서비스 이미지
│  ✅ 2개 태그 (version-sha, latest)
└──────────────────────────────────────────────┘
```

---

## 🎯 주요 기능

### 1. Matrix Strategy로 병렬 빌드
```yaml
strategy:
  matrix:
    service:
      - server-discovery
      - server-gateway
      - service-user
      - service-auction
      - service-payment
      - service-search
      - service-chat
      - service-admin
      - service-product
```

### 2. Jib로 Docker Daemon 없이 빌드
```bash
./gradlew :service-user:jib
```

### 3. Multi-architecture 지원
```kotlin
platforms {
    platform {
        architecture = "amd64"
        os = "linux"
    }
    
    platform {
        architecture = "arm64"
        os = "linux"
    }
}
```

### 4. 자동 태그 생성
```
auction-service-user:0.0.1-SNAPSHOT-abc12345
auction-service-user:latest
```

- **version-sha:** 특정 커밋 추적 가능
- **latest:** 항상 최신 버전

---

## 🔐 필수 설정

### GitHub Secrets

Repository → Settings → Secrets and variables → Actions

| Secret | 값                | 설명 |
|--------|------------------|----|
| `DOCKER_USERNAME` | `{username}`     | Docker Hub 사용자명 |
| `DOCKER_PASSWORD` | `{access_token}` | Docker Hub 액세스 토큰 |

### Docker Hub 액세스 토큰 생성

1. [Docker Hub](https://hub.docker.com/) 로그인
2. Account Settings → Security
3. New Access Token
4. Permissions: **Read, Write, Delete**
5. Generate 후 복사

---

## 🧪 테스트 방법

### 1. 로컬에서 Jib 빌드 테스트

```bash
# 로컬 Docker Daemon에 빌드
./gradlew :service-user:jibDockerBuild

# Docker 이미지 확인
docker images | grep auction

# 실행 테스트
docker run -p 8080:8080 auction-service-user:latest
```

### 2. Docker Hub에 Push 테스트

```bash
# 환경 변수 설정
export DOCKER_USERNAME={username}
export DOCKER_PASSWORD={access_token}

# Docker Hub에 Push
./gradlew :service-user:jib

# 성공 메시지 확인
# Built and pushed image as {username}/auction-service-user:latest
```

### 3. GitHub Actions 테스트

```bash
# main 브랜치에 Push
git add .
git commit -m "test: CI/CD 파이프라인 테스트"
git push origin main

# GitHub Actions 확인
# Repository → Actions 탭
```

---

## 🔗 관련 파일

### CI/CD 설정
- `.github/workflows/deploy.yml` - GitHub Actions workflow
- `build.gradle.kts` - Jib 플러그인 설정

### 서비스 모듈
- `server-discovery/build.gradle.kts` - Discovery 서비스 Jib 설정
- `server-gateway/build.gradle.kts` - Gateway 서비스 Jib 설정
- `service-user/build.gradle.kts` - User 서비스 Jib 설정
- `service-auction/build.gradle.kts` - Auction 서비스 Jib 설정
- `service-payment/build.gradle.kts` - Payment 서비스 Jib 설정
- `service-search/build.gradle.kts` - Search 서비스 Jib 설정
- `service-chat/build.gradle.kts` - Chat 서비스 Jib 설정
- `service-admin/build.gradle.kts` - Admin 서비스 Jib 설정
- `service-product/build.gradle.kts` - Product 서비스 Jib 설정

### 문서
- `CI_CD_GUIDE.md` - 전체 가이드
- `DOCKER_HUB_SETUP.md` - Docker Hub 설정

---

