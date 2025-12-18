# Product 기능 사용 가이드

## 📦 개요

판매자는 상품을 등록하고, 이미지를 업로드한 후, 해당 상품으로 경매를 생성할 수 있습니다.

## 🎯 주요 기능

### 1. 상품 등록
- 다중 이미지 업로드 (최대 10개)
- 드래그 앤 드롭으로 이미지 순서 변경
- 카테고리 및 상품 상태 선택
- 임시저장 상태로 등록

### 2. 상품 관리
- 내 상품 목록 조회
- 상품 상세 정보 확인
- 상품 이미지 갤러리
- 상품 삭제

### 3. 경매 생성
- 등록한 상품으로 경매 생성
- 시작가, 시작/종료 시간 설정
- 자동 상태 변경 (DRAFT → REGISTERED)

## 🚀 시작하기

### 1. 인프라 시작

```bash
cd /Users/chpark/Documents/ch/auction

docker-compose up -d

# MinIO 버킷이 자동 생성되었는지 확인
docker logs auction-minio-init
```

## 📱 화면 사용 가이드

### 1. 상품 등록

**URL:** `http://localhost/product-register.html`

1. 로그인 후 네비게이션 바에서 **"상품 등록"** 클릭
2. 상품 정보 입력:
   - 상품명
   - 카테고리 선택
   - 상품 상태 (새 상품/중고)
   - 상품 설명
3. 이미지 업로드:
   - **드래그 앤 드롭** 또는 **클릭하여 선택**
   - 최대 10개 이미지
   - 각 이미지 최대 10MB
   - 드래그하여 순서 변경 가능
4. **"상품 등록"** 버튼 클릭

### 2. 내 상품 목록

**URL:** `http://localhost/product-list.html`

- 등록한 모든 상품 조회
- 필터링:
  - 카테고리별
  - 상태별 (임시저장/경매등록됨/판매완료)
  - 키워드 검색
- 각 상품 카드에서:
  - **상세보기:** 상품 상세 페이지로 이동
  - **경매 등록:** 경매 생성 페이지로 이동 (DRAFT 상태만)
  - **삭제:** 상품 삭제

### 3. 상품 상세

**URL:** `http://localhost/product-detail.html?id={productId}`

- 메인 이미지 표시
- 썸네일 클릭하여 이미지 변경
- 상품 정보:
  - 카테고리, 상태, 등록일
  - 상세 설명
- 액션:
  - **경매 등록하기** (DRAFT 상태만)
  - **상품 삭제**

### 4. 경매 생성

**URL:** `http://localhost/auction-create.html?productId={productId}`

1. 상품 정보 확인 (미리보기)
2. 경매 설정:
   - 시작 가격
   - 경매 시작 시간
   - 경매 종료 시간 (시작 후 최소 1시간)
3. **"경매 등록"** 버튼 클릭
4. 자동으로 경매 상세 페이지로 이동

### 5. 마이페이지 - 판매 내역

**URL:** `http://localhost/mypage.html` → **"판매 내역"** 탭

- 내가 생성한 모든 경매 조회
- 경매 상태별 조회:
  - READY (경매 대기)
  - ONGOING (진행 중)
  - ENDED (종료)
- **상세보기** 버튼으로 경매 페이지 이동

## 🔧 API 엔드포인트

### Product API (service-product)

```
POST   /api/v1/products                      # 상품 생성
GET    /api/v1/products/{id}                 # 상품 조회
GET    /api/v1/products/seller/{sellerId}    # 판매자 상품 목록
POST   /api/v1/products/{id}/images          # 이미지 추가
DELETE /api/v1/products/{id}/images/{imageId} # 이미지 삭제
PATCH  /api/v1/products/{id}/status          # 상태 변경
DELETE /api/v1/products/{id}                 # 상품 삭제
```

### 수정된 Auction API (service-auction)

```
POST   /api/v1/seller/auctions               # 경매 생성 (productId 필수)
GET    /api/v1/seller/auctions/my-selling    # 내가 올린 경매 목록
```

## 🏗️ 아키텍처 변경사항

### 1. 신규 서비스: `service-product`

**책임:**
- 상품(Product) 도메인 관리
- 이미지 업로드/삭제 (MinIO S3)
- 상품 상태 관리 (DRAFT/REGISTERED/SOLD/DELETED)

### 2. MinIO Object Storage

**설정:**
```yaml
# docker-compose.yml
minio:
  image: minio/minio
  ports:
    - "9000:9000"   # S3 API
    - "9001:9001"   # Web Console
  environment:
    MINIO_ROOT_USER: admin
    MINIO_ROOT_PASSWORD: password
```

**접속:**
- API: http://localhost:9000
- Console: http://localhost:9001
- 자동 생성된 버킷: `auction-bucket`

### 3. 서비스 간 통신

**Feign Client 추가:**

```kotlin
@FeignClient(name = "service-product")
interface ProductClient {
    @GetMapping("/api/v1/products/{productId}")
    fun getProduct(@PathVariable productId: Long): ApiResponse<ProductResponse>
    
    @PatchMapping("/api/v1/products/{productId}/status")
    fun updateProductStatus(@PathVariable productId: Long, @RequestParam status: String)
}
```

**경매 생성 흐름:**
```
1. [Seller] 상품 등록 → service-product (status: DRAFT)
2. [Seller] 경매 생성 → service-auction
   - ProductClient로 상품 정보 조회
   - ProductClient로 상품 상태 변경 (REGISTERED)
   - Auction 생성 (productId, title, thumbnailUrl 저장)
3. [Kafka] AuctionCreatedEvent 발행
4. [service-search] Elasticsearch 인덱싱
   - ProductClient로 상품 상세 정보 조회
   - description, condition 정보로 인덱싱
```

### 4. 이벤트 기반 동기화

**Product 업데이트 시:**
```
1. [service-product] Product 정보 수정
2. [Kafka] ProductUpdatedEvent 발행
3. [service-search] 해당 productId를 가진 모든 Auction Document 업데이트
   - title, category, description 등 반영
```

## 🎨 프론트엔드 페이지

### 상품 페이지

1. **product-register.html**
   - 상품 등록 폼
   - 이미지 드래그 앤 드롭
   - 이미지 미리보기 및 순서 변경

2. **product-list.html**
   - 내 상품 목록 (그리드 레이아웃)
   - 필터링 및 검색
   - 상태별 배지 표시

3. **product-detail.html**
   - 상품 상세 정보
   - 이미지 갤러리 (메인 + 썸네일)
   - 경매 등록 버튼

4. **auction-create.html**
   - 상품 선택 → 경매 설정
   - 유효성 검사 (시작가, 시간 등)

## 🔍 모니터링

### 1. MinIO Console

**URL:** http://localhost:9001  
**Login:** admin / password

- 업로드된 이미지 파일 확인
- `auction-bucket/products/` 디렉토리

### 2. Zipkin UI (Distributed Tracing)

**URL:** http://localhost:9411

- 경매 생성 시 service-auction → service-product 호출 추적
- 검색 시 service-search → service-product 호출 추적

### 3. Kafka (이벤트 확인)

```bash
docker exec -it auction-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic product-update-topic \
  --from-beginning

docker exec -it auction-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic auction-create-topic \
  --from-beginning
```

### 4. Elasticsearch (인덱스 확인)

```bash
curl -X GET "http://localhost:9200/auctions/_search?pretty" \
  -H "Content-Type: application/json" \
  -d '{"query": {"match_all": {}}}'
```

## 📦 데이터 흐름

```
┌──────────────┐
│   Seller     
└──────┬───────┘
       │
       ▼
┌────────────────────────────────────────────────┐
│  1. Product 등록                                
│     POST /api/v1/products                      
└────────┬───────────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────────┐
│  service-product                                
│  - Product 저장 (MySQL)                        
│  - Images 업로드 (MinIO S3)                    
│  - Status: DRAFT                               
└────────────────────────────────────────────────┘
         │
         │ 2. 경매 생성
         ▼
┌────────────────────────────────────────────────┐
│  service-auction                               
│  - ProductClient.getProduct(productId)         
│  - Auction 생성                                 
│  - ProductClient.updateStatus(REGISTERED)      
│  - Kafka: AuctionCreatedEvent 발행              
└────────┬───────────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────────┐
│  service-search                                
│  - AuctionCreatedEvent 수신                    
│  - ProductClient.getProduct(productId)         
│  - AuctionDocument 생성 (Product 정보 포함)    
│  - Elasticsearch 인덱싱                        
└────────────────────────────────────────────────┘
         │
         │ 3. 검색
         ▼
┌────────────────────────────────────────────────┐
│  사용자                                          
│  - 키워드 검색 (title, description)               
│  - 카테고리 필터                                  
│  - 상품 상태 필터                                 
└────────────────────────────────────────────────┘
```

## 🚨 문제 해결

### MinIO 버킷이 생성되지 않았을 때

```bash
# minio-init 컨테이너 로그 확인
docker logs auction-minio-init

# 수동 버킷 생성
docker exec -it auction-minio mc alias set minio http://localhost:9000 admin password
docker exec -it auction-minio mc mb minio/auction-bucket
docker exec -it auction-minio mc policy set public minio/auction-bucket
```

### 이미지 업로드 실패

1. MinIO 서비스 확인:
   ```bash
   docker ps | grep minio
   curl http://localhost:9000/minio/health/live
   ```

2. service-product 로그 확인:
   ```bash
   # 로그에서 S3 관련 에러 확인
   grep -i "s3\|minio\|image" logs/service-product.log
   ```

### 경매 생성 시 "Product not found"

- Product ID가 유효한지 확인
- ProductClient Feign 연결 확인
- service-product가 실행 중인지 확인

---

