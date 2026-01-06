#!/bin/bash

echo "🚀 Starting complete copy of module-common files..."

# 모든 서비스 목록
ALL_SERVICES="user-service auction-service payment-service product-service search-service chat-service admin-service"
JPA_SERVICES="user-service auction-service payment-service product-service"
KAFKA_SERVICES="user-service auction-service payment-service product-service search-service chat-service"

# 1. 모든 서비스에 기본 파일 복사
echo "📦 Step 1: Copying common files to all services..."
for SERVICE in $ALL_SERVICES; do
  echo "  → $SERVICE"
  
  # 디렉토리 생성
  mkdir -p $SERVICE/src/main/kotlin/com/ch/auction/common
  mkdir -p $SERVICE/src/main/kotlin/com/ch/auction/exception
  
  # 기본 파일 복사
  cp -f module-common/src/main/kotlin/com/ch/auction/common/ApiResponse.kt $SERVICE/src/main/kotlin/com/ch/auction/common/
  cp -f module-common/src/main/kotlin/com/ch/auction/common/ErrorCode.kt $SERVICE/src/main/kotlin/com/ch/auction/common/
  cp -f module-common/src/main/kotlin/com/ch/auction/common/GlobalExceptionHandler.kt $SERVICE/src/main/kotlin/com/ch/auction/common/
  cp -f module-common/src/main/kotlin/com/ch/auction/exception/BusinessException.kt $SERVICE/src/main/kotlin/com/ch/auction/exception/
done

# 2. JPA 서비스에 BaseEntity 복사
echo "📦 Step 2: Copying BaseEntity to JPA services..."
for SERVICE in $JPA_SERVICES; do
  echo "  → $SERVICE"
  cp -f module-common/src/main/kotlin/com/ch/auction/common/BaseEntity.kt $SERVICE/src/main/kotlin/com/ch/auction/common/
done

# 3. Kafka 서비스에 Kafka 관련 파일 복사
echo "📦 Step 3: Copying Kafka files to Kafka services..."
for SERVICE in $KAFKA_SERVICES; do
  echo "  → $SERVICE"
  
  mkdir -p $SERVICE/src/main/kotlin/com/ch/auction/common/config
  mkdir -p $SERVICE/src/main/kotlin/com/ch/auction/common/event
  
  cp -f module-common/src/main/kotlin/com/ch/auction/common/config/KafkaConfig.kt $SERVICE/src/main/kotlin/com/ch/auction/common/config/
  cp -f module-common/src/main/kotlin/com/ch/auction/common/event/*.kt $SERVICE/src/main/kotlin/com/ch/auction/common/event/
done

# 4. 모든 서비스에 config, dto, enums 복사
echo "📦 Step 4: Copying config, dto, enums to all services..."
for SERVICE in $ALL_SERVICES; do
  echo "  → $SERVICE"
  
  mkdir -p $SERVICE/src/main/kotlin/com/ch/auction/common/config
  mkdir -p $SERVICE/src/main/kotlin/com/ch/auction/common/dto
  mkdir -p $SERVICE/src/main/kotlin/com/ch/auction/common/enums
  mkdir -p $SERVICE/src/main/kotlin/com/ch/auction/common/filter
  mkdir -p $SERVICE/src/main/kotlin/com/ch/auction/common/interceptor
  mkdir -p $SERVICE/src/main/kotlin/com/ch/auction/domain/repository
  
  cp -f module-common/src/main/kotlin/com/ch/auction/common/config/WebConfig.kt $SERVICE/src/main/kotlin/com/ch/auction/common/config/ 2>/dev/null || true
  cp -f module-common/src/main/kotlin/com/ch/auction/common/config/AsyncConfig.kt $SERVICE/src/main/kotlin/com/ch/auction/common/config/ 2>/dev/null || true
  cp -f module-common/src/main/kotlin/com/ch/auction/common/dto/*.kt $SERVICE/src/main/kotlin/com/ch/auction/common/dto/ 2>/dev/null || true
  cp -f module-common/src/main/kotlin/com/ch/auction/common/enums/*.kt $SERVICE/src/main/kotlin/com/ch/auction/common/enums/ 2>/dev/null || true
  cp -f module-common/src/main/kotlin/com/ch/auction/common/filter/*.kt $SERVICE/src/main/kotlin/com/ch/auction/common/filter/ 2>/dev/null || true
  cp -f module-common/src/main/kotlin/com/ch/auction/common/interceptor/*.kt $SERVICE/src/main/kotlin/com/ch/auction/common/interceptor/ 2>/dev/null || true
  cp -f module-common/src/main/kotlin/com/ch/auction/domain/repository/*.kt $SERVICE/src/main/kotlin/com/ch/auction/domain/repository/ 2>/dev/null || true
done

# 5. user-service에 security 관련 파일 복사
echo "📦 Step 5: Copying security files to user-service..."
mkdir -p user-service/src/main/kotlin/com/ch/auction/common/security/jwt
mkdir -p user-service/src/main/kotlin/com/ch/auction/domain/auth/port

cp -rf module-common/src/main/kotlin/com/ch/auction/common/security/* user-service/src/main/kotlin/com/ch/auction/common/security/
cp -rf module-common/src/main/kotlin/com/ch/auction/domain/auth/* user-service/src/main/kotlin/com/ch/auction/domain/auth/

echo "✅ Complete! All files copied successfully."
