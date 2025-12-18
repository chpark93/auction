# CI/CD 파이프라인 가이드

## 🚀 개요

GitHub Actions를 이용 CI/CD 파이프라인 구축

**주요 기능:**
- ✅ `main` 브랜치 Push 시 자동 테스트 & 빌드
- ✅ Jib를 사용한 Docker 이미지 빌드 (Docker Daemon 불필요)
- ✅ Docker Hub에 자동 Push
- ✅ Multi-architecture 지원 (amd64, arm64)

---

## 📋 워크플로우 구조

### `.github/workflows/deploy.yml`

```
┌─────────────────────────────────────────────┐
│  1. test-and-build
│     - 코드 체크아웃
│     - JDK 21 설정
│     - Gradle 테스트 실행
│     - Gradle 빌드 (JAR 생성)
│     - 테스트 결과 업로드
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│  2. docker-build-push (main 브랜치)
│     - 서비스 병렬 빌드 (Matrix Strategy)
│     - Docker Hub 로그인
│     - Jib로 Docker 이미지 빌드
│     - Docker Hub에 Push
│     - 태그: version-sha, latest
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│  3. docker-build-frontend (main 브랜치)
│     - Frontend Nginx 이미지 빌드
│     - Docker Hub에 Push
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│  4. notify
│     - 성공/실패 여부 확인
└─────────────────────────────────────────────┘
```

---

## 🔧 설정 방법

### 1. GitHub Repository Secrets 설정

GitHub Repository → Settings → Secrets and variables → Actions → New repository secret

#### 필수 Secrets

| Secret 이름 | 설명 | 예시              |
|-------------|------|-----------------|
| `DOCKER_USERNAME` | Docker Hub 사용자명 | `{username}`    |
| `DOCKER_PASSWORD` | Docker Hub 액세스 토큰 | `{acess_token}` |

#### Docker Hub 액세스 토큰 생성 방법

1. [Docker Hub](https://hub.docker.com/) 로그인
2. 우측 상단 프로필 → **Account Settings**
3. **Security** → **New Access Token**
4. Token Description 입력 (예: `github-actions-ci`)
5. Access permissions: **Read, Write, Delete**
6. **Generate** 클릭
7. 생성된 토큰 복사하여 GitHub Secrets에 저장

---

### 2. Gradle Jib 플러그인 설정

#### Root `build.gradle.kts`

```kotlin
plugins {
    id("com.google.cloud.tools.jib") version "3.4.0" apply false
}
```

#### 각 서비스 모듈 `build.gradle.kts`

```kotlin
plugins {
    id("com.google.cloud.tools.jib")
}

jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
    }
    to {
        image = System.getenv("DOCKER_IMAGE_PREFIX")?.let { "$it-service-user" }
            ?: "auction-service-user"
    }
    container {
        jvmFlags = listOf(
            "-Xms512m",
            "-Xmx1024m",
            "-XX:+UseContainerSupport",
            "-XX:MaxRAMPercentage=75.0"
        )
        ports = listOf("8080")
        environment = mapOf(
            "SPRING_PROFILES_ACTIVE" to "prod"
        )
    }
}
```

---

## 🚀 사용 방법

### 1. 자동 CI/CD (Push)

```bash
# 코드 변경 후 main 브랜치에 Push
git add .
git commit -m "feat: 새로운 기능 추가"
git push origin main
```

**자동으로 실행되는 작업:**
1. ✅ 전체 테스트 실행
2. ✅ Gradle 빌드
3. ✅ Docker 이미지 빌드
4. ✅ Docker Hub에 Push

### 2. Pull Request 시

```bash
# feature 브랜치에서 작업
git checkout -b feature/new-feature
git add .
git commit -m "feat: 새로운 기능"
git push origin feature/new-feature
```

**PR 생성 시 자동으로 실행:**
1. ✅ 전체 테스트 실행
2. ✅ Gradle 빌드
3. ❌ Docker 이미지는 빌드하지 않음 (main 브랜치만)

---

## 🐳 로컬에서 Jib 빌드 테스트

### 1. 특정 서비스 이미지 빌드

```bash
# Docker Hub에 Push하지 않고 로컬 Docker Daemon에만 빌드
./gradlew :service-user:jibDockerBuild

# Docker 이미지 확인
docker images | grep auction
```

### 2. 모든 서비스 빌드 (Docker Hub에 Push)

```bash
# Docker Hub 로그인 필요
export DOCKER_USERNAME={username}
export DOCKER_PASSWORD={access_token}

./gradlew jib
```

### 3. 커스텀 이미지 이름으로 빌드

```bash
./gradlew :service-user:jib \
  -Djib.to.image=myregistry/my-service:1.0.0
```

---

## 🎨 Jib의 장점

### 1. Docker Daemon 불필요
- ✅ GitHub Actions 환경에서 바로 이미지 빌드 가능
- ✅ Docker Desktop 없이도 빌드 가능

### 2. 빠른 빌드 속도
- ✅ Layer 캐싱으로 변경된 부분만 빌드
- ✅ 평균 3-5분으로 빌드 완료

### 3. 최적화된 이미지
- ✅ Distroless/Alpine 기반으로 작은 이미지 크기
- ✅ 보안 취약점 최소화

### 4. 재현 가능한 빌드
- ✅ 항상 동일한 환경에서 빌드
- ✅ 타임스탬프 고정으로 동일한 이미지 생성

---

## 🔧 고급 설정

### 1. 빌드 캐시 최적화

`.github/workflows/deploy.yml`에서 Gradle 캐싱이 자동으로 설정됨:

```yaml
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'temurin'
    cache: gradle  # 자동 캐싱
```

### 2. 병렬 빌드 (Matrix Strategy)

```yaml
strategy:
  matrix:
    service:
      - server-discovery
      - server-gateway
      - service-user
      # ...
```

### 3. 조건부 실행

```yaml
if: github.event_name == 'push' && github.ref == 'refs/heads/main'
```

- `main` 브랜치 Push 시에만 Docker 이미지 빌드
- PR이나 다른 브랜치는 테스트만 수행

---

