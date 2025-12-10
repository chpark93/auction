# 🗄️ MSA 독립 데이터베이스 설정 가이드

## 📋 개요

MSA 원칙에 따라 각 마이크로서비스가 **독립적인 MySQL 데이터베이스**를 사용하도록 설정되었습니다.

## 🏗️ 데이터베이스 구조

```
MySQL Container (Port 3306)
├── users       → service-user
├── auctions    → service-auction
└── payments    → service-payment
```

## 📂 파일 구조

```
auction/
├── mysql-init/
│   └── init.sql                    # 데이터베이스 초기화 스크립트
├── docker-compose.yml              # MySQL 볼륨 마운트 설정
├── service-user/
│   └── src/main/resources/
│       └── application.yml         # users database 연결 설정
├── service-auction/
│   └── src/main/resources/
│       └── application.yml         # auctions database 연결 설정
└── service-payment/
    └── src/main/resources/
        └── application.yml         # payments database 연결 설정
```

## 🔧 설정 상세

### 1. MySQL 초기화 스크립트 (`mysql-init/init.sql`)

```sql
CREATE DATABASE IF NOT EXISTS users CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS auctions CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS payments CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**특징:**
- 컨테이너 최초 실행 시 자동 실행
- `/docker-entrypoint-initdb.d` 디렉토리에 마운트

### 2. Docker Compose 설정

```yaml
mysql:
  image: mysql:8.0
  container_name: auction-mysql
  environment:
    MYSQL_ROOT_PASSWORD: password
  volumes:
    - ./mysql-init:/docker-entrypoint-initdb.d  # 초기화 스크립트
    - mysql_data:/var/lib/mysql                 # 데이터 영속성
  ports:
    - "3306:3306"
```

**환경 변수 (각 서비스별):**

| 서비스 | 데이터베이스   | URL                                |
|--------|----------|------------------------------------|
| service-user | users    | `jdbc:mysql://mysql:3306/users`    |
| service-auction | auctions | `jdbc:mysql://mysql:3306/auctions` |
| service-payment | payments | `jdbc:mysql://mysql:3306/payments` |

### 3. 서비스별 application.yml 설정

#### A. service-user

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/users?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

**엔티티:**
- `User` (users 테이블)
- `Address` (addresses 테이블)
- `VerificationInfo` (verification_info 테이블)

#### B. service-auction

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auctions?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

**엔티티:**
- `Auction` (auctions 테이블)
- `Bid` (bids 테이블)
- `shedlock` (분산 락 테이블)

#### C. service-payment

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/payments?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

**엔티티:**
- `Payment` (payments 테이블)
- `Order` (orders 테이블)
- `Delivery` (deliveries 테이블)
- `PointTransaction` (point_transactions 테이블)
- `shedlock` (분산 락 테이블)

## 🚀 실행 방법

### 1. Docker Compose로 MySQL 시작

```bash
# MySQL 컨테이너 시작 (초기화 스크립트 자동 실행)
docker compose up -d mysql

# 데이터베이스 생성 확인
docker exec -it auction-mysql mysql -uroot -ppassword -e "SHOW DATABASES;"
```

**예상 출력:**
```
+--------------------+
| Database           |
+--------------------+
| auctions           |
| information_schema |
| mysql              |
| payments           |
| performance_schema |
| sys                |
| users              |
+--------------------+
```

### 2. 각 서비스 실행

```bash
# IntelliJ IDEA에서 각 서비스 실행
# 또는 Gradle로 실행
./gradlew :service-user:bootRun
./gradlew :service-auction:bootRun
./gradlew :service-payment:bootRun
```

### 3. 테이블 생성 확인

```bash
# user_db 테이블 확인
docker exec -it auction-mysql mysql -uroot -ppassword -e "USE users; SHOW TABLES;"

# auction_db 테이블 확인
docker exec -it auction-mysql mysql -uroot -ppassword -e "USE auctions; SHOW TABLES;"

# payment_db 테이블 확인
docker exec -it auction-mysql mysql -uroot -ppassword -e "USE payments; SHOW TABLES;"
```

## 🔍 데이터베이스 격리 확인

### 1. 엔티티 격리

각 서비스는 **자신의 도메인 엔티티만** 포함:

| 서비스 | 엔티티 | 외부 참조 |
|--------|--------|----------|
| service-user | User, Address | ❌ 없음 |
| service-auction | Auction, Bid | `userId: Long` (FK 없음) |
| service-payment | Payment, Order, Delivery | `userId: Long`, `auctionId: Long` (FK 없음) |

**중요:** 
- 다른 서비스의 엔티티를 JPA Entity로 참조하지 않음
- 외래 키(Foreign Key) 대신 `Long` 타입 ID만 저장
- 데이터 정합성은 **Saga 패턴**과 **Kafka 이벤트**로 관리

### 2. 서비스 간 통신

```
service-auction (입찰)
    ↓ Feign Client
service-user (사용자 정보 조회)

service-auction (경매 종료)
    ↓ Kafka Event
service-payment (결제 처리)
```

## 🛠️ 의존성 확인

각 서비스의 `build.gradle.kts`에 MySQL 드라이버 포함:

```kotlin
dependencies {
    // MySQL Driver
    runtimeOnly("com.mysql:mysql-connector-j")
    
    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
```

**확인 방법:**
```bash
grep -r "mysql-connector" service-*/build.gradle.kts
```

## 📊 데이터베이스 스키마 관리

### DDL 자동 생성 (개발 환경)

```yaml
jpa:
  hibernate:
    ddl-auto: update
```

### 운영 환경 권장 설정

```yaml
jpa:
  hibernate:
    ddl-auto: validate
```

## 🔐 보안 고려사항

### 개발 환경 (현재)

```yaml
username: root
password: password
```

### 운영 환경

```sql
-- 서비스별 전용 계정 생성
CREATE USER 'user_service'@'%' IDENTIFIED BY 'chpark_password';
GRANT ALL PRIVILEGES ON users.* TO 'user_service'@'%';

CREATE USER 'auction_service'@'%' IDENTIFIED BY 'chpark_password';
GRANT ALL PRIVILEGES ON auctions.* TO 'auction_service'@'%';

CREATE USER 'payment_service'@'%' IDENTIFIED BY 'chpark_password';
GRANT ALL PRIVILEGES ON payments.* TO 'payment_service'@'%';

FLUSH PRIVILEGES;
```

## 📈 모니터링

### 데이터베이스 크기 확인

```sql
SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema IN ('users', 'auctions', 'payments')
GROUP BY table_schema;
```

### 테이블별 레코드 수

```sql
SELECT 
    table_schema,
    table_name,
    table_rows
FROM information_schema.tables
WHERE table_schema IN ('users', 'auctions', 'payments')
ORDER BY table_schema, table_name;
```

## 🎯 MSA 데이터베이스 원칙 준수

✅ **Database per Service Pattern**
- 각 서비스가 독립적인 데이터베이스 소유
- 다른 서비스의 DB에 직접 접근 불가

✅ **Loose Coupling**
- JPA Entity 간 직접 참조 없음
- ID만 저장하여 느슨한 결합 유지

✅ **Service Autonomy**
- 각 서비스가 독립적으로 배포 가능
- 스키마 변경이 다른 서비스에 영향 없음

✅ **Data Consistency**
- Saga 패턴으로 분산 트랜잭션 관리
- Kafka 이벤트로 데이터 동기화

---

**© 2025 Auction System - ChPark**

