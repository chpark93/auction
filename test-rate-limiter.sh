#!/bin/bash

# Rate Limiter 테스트 스크립트
# 
# 사용법:
#   chmod +x test-rate-limiter.sh
#   ./test-rate-limiter.sh

echo "========================================="
echo "Rate Limiter 테스트 시작"
echo "========================================="
echo ""

# 설정
GATEWAY_URL="http://localhost:8081"
USER_ID="test-user-1"
TOKEN="test-token"

# 색상 코드
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 테스트 1: 입찰 API Rate Limit (초당 5개 제한)
echo "========================================="
echo "테스트 1: 입찰 API Rate Limit"
echo "정책: 초당 5개, 최대 10개 버스트"
echo "========================================="
echo ""

SUCCESS_COUNT=0
RATE_LIMITED_COUNT=0

for i in {1..15}; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
        -X POST "$GATEWAY_URL/api/v1/auctions/1/bid" \
        -H "Content-Type: application/json" \
        -H "X-User-Id: $USER_ID" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{"amount": 10000}')
    
    if [ "$HTTP_CODE" = "429" ]; then
        echo -e "${RED}요청 $i: HTTP $HTTP_CODE - Rate Limit 초과${NC}"
        RATE_LIMITED_COUNT=$((RATE_LIMITED_COUNT + 1))
    else
        echo -e "${GREEN}요청 $i: HTTP $HTTP_CODE - 성공 (또는 다른 에러)${NC}"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
    fi
    
    sleep 0.1
done

echo ""
echo "결과:"
echo "  - 성공: $SUCCESS_COUNT"
echo "  - Rate Limit 초과: $RATE_LIMITED_COUNT"
echo ""

# 1초 대기 (토큰 충전)
echo "1초 대기 중 (토큰 충전)..."
sleep 1
echo ""

# 테스트 2: 토큰 복구 확인
echo "========================================="
echo "테스트 2: Rate Limit 복구 확인"
echo "========================================="
echo ""

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$GATEWAY_URL/api/v1/auctions/1/bid" \
    -H "Content-Type: application/json" \
    -H "X-User-Id: $USER_ID" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"amount": 10000}')

if [ "$HTTP_CODE" = "429" ]; then
    echo -e "${RED}복구 실패: 여전히 Rate Limit 상태${NC}"
else
    echo -e "${GREEN}복구 성공: 다시 요청 가능 (HTTP $HTTP_CODE)${NC}"
fi
echo ""

# 테스트 3: 다른 사용자는 독립적인 Rate Limit
echo "========================================="
echo "테스트 3: 사용자별 독립적인 Rate Limit"
echo "========================================="
echo ""

USER_2="test-user-2"

echo "User 1으로 10번 요청..."
for i in {1..10}; do
    curl -s -o /dev/null \
        -X POST "$GATEWAY_URL/api/v1/auctions/1/bid" \
        -H "Content-Type: application/json" \
        -H "X-User-Id: $USER_ID" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{"amount": 10000}'
    sleep 0.05
done

echo "User 2로 요청 (독립적으로 처리되어야 함)..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$GATEWAY_URL/api/v1/auctions/1/bid" \
    -H "Content-Type: application/json" \
    -H "X-User-Id: $USER_2" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"amount": 10000}')

if [ "$HTTP_CODE" = "429" ]; then
    echo -e "${RED}실패: User 2도 Rate Limit 적용됨 (잘못된 동작)${NC}"
else
    echo -e "${GREEN}성공: User 2는 독립적으로 처리됨 (HTTP $HTTP_CODE)${NC}"
fi
echo ""

# 테스트 4: 검색 API Rate Limit (초당 20개 제한)
echo "========================================="
echo "테스트 4: 검색 API Rate Limit"
echo "정책: 초당 20개, 최대 50개 버스트"
echo "========================================="
echo ""

SEARCH_SUCCESS=0
SEARCH_LIMITED=0

for i in {1..25}; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
        -X GET "$GATEWAY_URL/api/v1/search/auctions?keyword=test")
    
    if [ "$HTTP_CODE" = "429" ]; then
        SEARCH_LIMITED=$((SEARCH_LIMITED + 1))
    else
        SEARCH_SUCCESS=$((SEARCH_SUCCESS + 1))
    fi
    
    sleep 0.05
done

echo "검색 결과:"
echo "  - 성공: $SEARCH_SUCCESS"
echo "  - Rate Limit 초과: $SEARCH_LIMITED"
echo ""

# 테스트 5: 429 응답 형식 확인
echo "========================================="
echo "테스트 5: 429 응답 형식 확인"
echo "========================================="
echo ""

# Rate Limit에 도달하도록 여러 번 요청
for i in {1..15}; do
    curl -s -o /dev/null \
        -X POST "$GATEWAY_URL/api/v1/auctions/1/bid" \
        -H "Content-Type: application/json" \
        -H "X-User-Id: test-user-3" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{"amount": 10000}'
    sleep 0.05
done

# 429 응답 확인
echo "429 응답 내용:"
curl -s -X POST "$GATEWAY_URL/api/v1/auctions/1/bid" \
    -H "Content-Type: application/json" \
    -H "X-User-Id: test-user-3" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"amount": 10000}' \
    -w "\nHTTP Status: %{http_code}\n" \
    | jq '.' 2>/dev/null || cat

echo ""
echo "========================================="
echo "테스트 완료"
echo "========================================="
echo ""
echo "참고:"
echo "  - 실제 Rate Limit 정책은 application.yml에서 확인"
echo "  - Redis 키 확인: docker exec -it redis redis-cli KEYS 'request_rate_limiter*'"
echo "  - Gateway 로그: grep 'Rate limit' logs/gateway.log"

