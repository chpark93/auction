# Rate Limiter 가이드

## 🛡️ 개요

server-gateway에 **Redis 기반 분산 Rate Limiting** 기능  
시스템을 DDoS 공격, 악의적인 요청, 과도한 트래픽으로부터 보호합니다.

---

## 🎯 주요 기능

### 1. 분산 환경 지원
- **Redis**를 사용하여 여러 Gateway 인스턴스가 Rate Limit 카운터를 공유
- 정확한 요청 제한

### 2. 유연한 KeyResolver
- **사용자 기반** (`userKeyResolver`) - 로그인 유저별 제한
- **IP 기반** (`ipKeyResolver`) - 익명 사용자 IP별 제한
- **경로 기반** (`pathKeyResolver`) - API 엔드포인트별 제한
- **조합 기반** (`compositeKeyResolver`) - 사용자 + IP 조합

### 3. API별 차별화된 정책
- **입찰 API:** 초당 5개
- **판매자 API:** 초당 10개
- **검색 API:** 초당 20개

---

## 📊 Rate Limit 정책

### 입찰 API (POST /api/v1/auctions/**/bid)
```yaml
replenishRate: 5      # 초당 5개 토큰 충전
burstCapacity: 10     # 최대 10개까지 버스트 허용
```

**의미:**
- 평균적으로 1초에 5번 입찰 가능
- 갑자기 10번까지는 허용 (버스트)
- 11번째 요청부터 `429 Too Many Requests` 반환

**예시:**
```
시간 0초: 10번 연속 입찰 → 모두 성공 (버스트 허용)
시간 1초: 5번 입찰 → 모두 성공 (토큰 5개 충전됨)
시간 1초: 6번째 입찰 시도 → 429 에러
시간 2초: 다시 5번 입찰 가능
```

### 판매자 API (POST /api/v1/seller/**)
```yaml
replenishRate: 10
burstCapacity: 20
```

### 검색 API (GET /api/v1/search/**)
```yaml
replenishRate: 20
burstCapacity: 50
```

---

## 🔧 설정

### application.yml

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
  
  cloud:
    gateway:
      # 전역 기본 설정
      redis-rate-limiter:
        replenishRate: 10
        burstCapacity: 20
        requestedTokens: 1
      
      routes:
        # 입찰 API - 엄격한 제한
        - id: service-auction-bid
          uri: lb://SERVICE-AUCTION
          predicates:
            - Path=/api/v1/auctions/**
            - Method=POST,PUT,DELETE
          filters:
            - AuthorizationHeaderFilter
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 5
                redis-rate-limiter.burstCapacity: 10
                key-resolver: "#{@userKeyResolver}"
                deny-empty-key: false
```

### KeyResolver 선택

```kotlin
// 1. 사용자 기반 (Primary - 기본값)
// X-User-Id 헤더 사용, 없으면 IP로 fallback
'key-resolver: "#{@userKeyResolver}"'

// 2. IP 기반
// 클라이언트 IP 주소 기반
'key-resolver: "#{@ipKeyResolver}"'

// 3. 경로 기반
// 사용자 + API 경로 조합
'key-resolver: "#{@pathKeyResolver}"'

// 4. 조합 기반
// 사용자 + IP 조합 (더 엄격)
'key-resolver: "#{@compositeKeyResolver}"'
```

---

## 📡 429 에러 응답

### HTTP 헤더
```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/json
Retry-After: 1
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1703123456789
```

### 응답 Body
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "R001",
    "message": "요청 횟수 제한을 초과했습니다. 잠시 후 다시 시도해주세요."
  }
}
```

---

## 🚀 사용 방법

### 1. Redis 시작

```bash
docker-compose up -d redis
# 또는 직접 실행
docker run -d -p 6379:6379 --name redis redis:7-alpine
```

### 2. Gateway 시작

```bash
./gradlew :server-gateway:bootRun
```

### 3. Rate Limit 테스트

#### 입찰 요청 테스트
```bash
# 같은 사용자로 연속 요청
for i in {1..15}; do
  echo "Request $i:"
  curl -X POST http://localhost:8081/api/v1/auctions/1/bid \
    -H "Content-Type: application/json" \
    -H "X-User-Id: 1" \
    -H "Authorization: Bearer YOUR_TOKEN" \
    -d '{"amount": 10000}' \
    -w "\nHTTP Status: %{http_code}\n\n"
  sleep 0.1
done
```

**예상 결과:**
- 1~10번째: 200 또는 다른 비즈니스 에러
- 11번째 이후: 429 Too Many Requests

#### 검색 요청 테스트
```bash
for i in {1..25}; do
  echo "Search $i:"
  curl -X GET "http://localhost:8081/api/v1/search/auctions?keyword=test" \
    -w "\nHTTP Status: %{http_code}\n\n"
  sleep 0.05
done
```

---

## 🔍 모니터링

### Redis에서 Rate Limit 키 확인

```bash
docker exec -it redis redis-cli

# Rate Limit 키 조회
KEYS request_rate_limiter*

# 특정 사용자의 Rate Limit 상태 확인
GET request_rate_limiter.user:1.tokens
GET request_rate_limiter.user:1.timestamp
```

### Gateway 로그 확인

```bash
# Rate Limit 경고 로그
grep "Rate limit exceeded" logs/gateway.log

# 예시 출력
2025-10-15 10:30:45 WARN  RateLimitExceededFilter - Rate limit exceeded for request: POST /api/v1/auctions/1/bid from 192.168.1.100
```

---

## ⚙️ 커스터마이징

### 1. 시간대별 다른 정책

```kotlin
@Bean
fun timeBasedKeyResolver(): KeyResolver {
    return KeyResolver { exchange ->
        val userId = exchange.request.headers.getFirst("X-User-Id")
        val hour = LocalTime.now().hour
        
        // 피크 시간대(12-14시, 18-20시)는 더 엄격하게
        val isPeakHour = hour in 12..14 || hour in 18..20
        val prefix = if (isPeakHour) "peak" else "normal"
        
        Mono.just("$prefix:user:$userId")
    }
}
```

---

## 🧪 테스트

### 단위 테스트

```kotlin
@Test
fun return_429_when_rate_limit_exceeded() {
    // Given: Rate Limit 설정
    val userId = "test-user-1"
    
    // When: 제한 초과 요청
    repeat(15) { index ->
        val result = webTestClient
            .post()
            .uri("/api/v1/auctions/1/bid")
            .header("X-User-Id", userId)
            .exchange()
        
        // Then: 11번째부터 429 반환
        if (index < 10) {
            result.expectStatus().isNotEqualTo(429)
        } else {
            result.expectStatus().isEqualTo(429)
        }
    }
}
```

### 실행

```bash
./gradlew :server-gateway:test --tests "*RateLimiterTest"
```

---

## 📈 성능 고려사항

### Redis 연결 풀 설정

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 20    # 최대 연결 수
          max-idle: 10      # 유휴 연결 수
          min-idle: 5       # 최소 유휴 연결
```

### Redis 클러스터 사용

```yaml
spring:
  data:
    redis:
      cluster:
        nodes:
          - redis-1:6379
          - redis-2:6379
          - redis-3:6379
```

### Rate Limit 키 TTL

Rate Limit 키는 자동으로 만료됩니다:
- `replenishRate: 10`이면 약 1~2초 TTL
- 메모리 부담 최소화

---

## 🚨 문제 해결

### Redis 연결 실패

```bash
# Redis 상태 확인
docker ps | grep redis

# Redis 로그 확인
docker logs redis

# Gateway 로그 확인
grep "Redis" logs/gateway.log
```

### Rate Limit이 작동하지 않음

1. **Redis 연결 확인**
   ```bash
   redis-cli ping
   # 응답: PONG
   ```

2. **KeyResolver 빈 등록 확인**
   ```bash
   grep "userKeyResolver" logs/gateway.log
   ```

3. **필터 순서 확인**
   - RequestRateLimiter가 AuthorizationHeaderFilter 뒤에 있어야 함

### 429 에러가 너무 자주 발생

```yaml
# replenishRate와 burstCapacity 증가
redis-rate-limiter:
  replenishRate: 20  # 더 여유롭게
  burstCapacity: 40
```

---

## 📝 베스트 프랙티스

### 1. 점진적 적용
```
1단계: 모니터링만 (로그만 남기고 차단 안 함)
2단계: 관대한 정책으로 시작 (replenishRate: 100)
3단계: 데이터 분석 후 적절한 값으로 조정
4단계: 최종 정책 적용
```

### 2. 사용자 경험 고려
- 429 에러 발생 시 명확한 안내 메시지
- `Retry-After` 헤더 제공
- 클라이언트 측 재시도 로직

### 3. 화이트리스트
```kotlin
// 특정 사용자는 Rate Limit 제외
@Bean
fun whitelistKeyResolver(): KeyResolver {
    val whitelist = setOf("admin-user", "monitoring-service")
    
    return KeyResolver { exchange ->
        val userId = exchange.request.headers.getFirst("X-User-Id")
        
        if (userId in whitelist) {
            Mono.just("whitelist:$userId") // 별도 정책
        } else {
            Mono.just("user:$userId")
        }
    }
}
```

---

