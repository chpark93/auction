# Distributed Tracing with Zipkin

## 아키텍처

```
Gateway → Service-Auction → Service-User (Feign)
                          → Service-Product (Feign)
                          → Kafka → Service-Search
                          → Kafka → Service-Payment
```

## 설정

### 1. Zipkin 서버
- **URL**: http://localhost:9411
- **Storage**: In-Memory (Develop)
- **Docker Compose**:
  ```yaml
  zipkin:
    image: openzipkin/zipkin
    container_name: auction-zipkin
    ports:
      - "9411:9411"
    environment:
      - STORAGE_TYPE=mem
  ```

### 2. 의존성 (module-common)
```kotlin
implementation("io.micrometer:micrometer-tracing-bridge-brave")
implementation("io.zipkin.reporter2:zipkin-reporter-brave")
```

### 3. 설정 (모든 서비스 공통)
```yaml
management:
  tracing:
    sampling:
      probability: 1.0 # 운영의 경우 낮춰야한다.
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans

logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

## 주요 기능

### 1. **TraceId & SpanId**
- **TraceId**: 전체 요청 흐름을 식별하는 고유 ID
- **SpanId**: 각 서비스 호출을 식별하는 고유 ID
- 로그에 자동으로 출력됩니다:
  ```
  INFO [service-auction,64f3a2b1c9e8d4f0,64f3a2b1c9e8d4f0] Processing bid request
  INFO [service-user,64f3a2b1c9e8d4f0,a1b2c3d4e5f6g7h8] Holding user points
  ```

### 2. **서비스 간 추적**
- **Feign Client**: HTTP 요청 시 자동으로 TraceId 전파
- **Kafka**: 메시지 헤더에 TraceId 포함하여 비동기 처리 추적
- **Gateway**: 모든 요청의 시작점, TraceId 생성

### 3. **성능 분석**
- 각 서비스의 응답 시간 측정
- 병목 구간 식별
- 에러 발생 지점 추적

## 사용 방법

### 1. Zipkin UI 접속
```bash
open http://localhost:9411
```

### 2. 추적 시나리오 예시

#### **시나리오 1: 입찰 요청**
1. 사용자가 경매에 입찰
2. Gateway → Service-Auction
3. Service-Auction → Service-User (포인트 확인)
4. Service-Auction → Redis (입찰 처리)
5. Service-Auction → Kafka (BidSuccessEvent 발행)
6. Kafka → Service-Search (ES 업데이트)

**Zipkin에서 확인:**
- 전체 요청 시간
- 각 서비스별 소요 시간
- Feign 호출 시간
- Kafka 메시지 처리 시간

#### **시나리오 2: 경매 종료**
1. Scheduler가 경매 종료 처리
2. Service-Auction → Kafka (AuctionEndedEvent 발행)
3. Kafka → Service-Payment (결제 처리)
4. Service-Payment → Service-User (포인트 차감/환불)
5. Kafka → Service-Search (ES 상태 업데이트)

**Zipkin에서 확인:**
- 비동기 이벤트 흐름
- 각 Consumer의 처리 시간
- 에러 발생 시 실패 지점

## 프로덕션 고려사항

### 1. **Sampling Rate 조정**
개발 환경에서는 100% 추적하지만, 프로덕션에서는 성능을 위해 샘플링 비율을 낮춰야 한다:
```yaml
management:
  tracing:
    sampling:
      probability: 0.1  # 10%만 추적
```

### 2. **Storage 변경**
In-Memory 대신 영구 저장소 사용:
- **Elasticsearch**: 대용량 트레이스 데이터 저장 및 검색
- **Cassandra**: 높은 쓰기 성능
- **MySQL**: 간단한 설정

```yaml
# Zipkin with Elasticsearch
zipkin:
  image: openzipkin/zipkin
  environment:
    - STORAGE_TYPE=elasticsearch
    - ES_HOSTS=elasticsearch:9200
```

### 3. **보안**
- Zipkin UI 접근 제한 (인증 추가)
- 민감한 데이터 마스킹 (비밀번호, 토큰 등)

### 4. **성능 최적화**
- 비동기 리포팅 사용
- Batch 전송으로 네트워크 오버헤드 감소
- Span 크기 제한

## 트러블슈팅

### 1. Trace가 보이지 않을 때
- Zipkin 서버 상태 확인: `docker logs auction-zipkin`
- 서비스 로그에서 TraceId 확인
- `management.zipkin.tracing.endpoint` 설정 확인

### 2. TraceId가 연결되지 않을 때
- Feign Client fallback 실행 시 TraceId 전파 확인
- Kafka 메시지 헤더에 TraceId 포함 여부 확인
- Spring Cloud Sleuth 의존성 충돌 확인

### 3. 성능 저하
- Sampling rate 낮추기
- Zipkin 서버 리소스 증설
- 비동기 리포팅 확인

