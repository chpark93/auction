#!/bin/bash

echo "=========================================="
echo "Distributed Tracing Test Script"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Gateway URL
GATEWAY_URL="http://localhost:8081"
ZIPKIN_URL="http://localhost:9411"

echo -e "${BLUE}1. Zipkin 서버 상태 확인${NC}"
if curl -s -o /dev/null -w "%{http_code}" "$ZIPKIN_URL/health" | grep -q "200"; then
    echo -e "${GREEN}✓ Zipkin is running${NC}"
else
    echo -e "${YELLOW}⚠ Zipkin may not be ready yet${NC}"
fi
echo ""

echo -e "${BLUE}2. 사용자 로그인 (TraceId 생성)${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@test.com",
    "password": "password123"
  }')

ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | sed 's/"accessToken":"//')

if [ -n "$ACCESS_TOKEN" ]; then
    echo -e "${GREEN}✓ Login successful${NC}"
    echo "Access Token: ${ACCESS_TOKEN:0:20}..."
else
    echo -e "${YELLOW}⚠ Login failed or user doesn't exist${NC}"
    echo "Response: $LOGIN_RESPONSE"
fi
echo ""

echo -e "${BLUE}3. 경매 목록 조회 (Gateway → Service-Auction → Redis)${NC}"
AUCTION_LIST=$(curl -s "$GATEWAY_URL/api/v1/auctions?page=0&size=5")
echo "Response: ${AUCTION_LIST:0:100}..."
echo ""

if [ -n "$ACCESS_TOKEN" ]; then
    echo -e "${BLUE}4. 사용자 포인트 조회 (Gateway → Service-Payment → Service-User)${NC}"
    POINT_RESPONSE=$(curl -s "$GATEWAY_URL/api/v1/users/points" \
      -H "Authorization: Bearer $ACCESS_TOKEN")
    echo "Response: ${POINT_RESPONSE:0:100}..."
    echo ""

    echo -e "${BLUE}5. 경매 상세 조회 (Gateway → Service-Auction → Service-Product + Service-User)${NC}"
    AUCTION_DETAIL=$(curl -s "$GATEWAY_URL/api/v1/auctions/1" \
      -H "Authorization: Bearer $ACCESS_TOKEN")
    echo "Response: ${AUCTION_DETAIL:0:100}..."
    echo ""
fi

echo -e "${BLUE}6. 검색 API 호출 (Gateway → Service-Search → Elasticsearch)${NC}"
SEARCH_RESPONSE=$(curl -s "$GATEWAY_URL/api/v1/search/auctions?keyword=test&page=0&size=5")
echo "Response: ${SEARCH_RESPONSE:0:100}..."
echo ""

echo "=========================================="
echo -e "${GREEN}테스트 완료!${NC}"
echo "=========================================="
echo ""
echo -e "${YELLOW}Zipkin UI에서 확인하세요:${NC}"
echo "URL: $ZIPKIN_URL"
echo ""
echo -e "${BLUE}확인 방법:${NC}"
echo "1. Zipkin UI 접속: open $ZIPKIN_URL"
echo "2. 'Find Traces' 버튼 클릭"
echo "3. Service Name 필터: server-gateway, service-auction, service-user 등"
echo "4. 최근 Trace 클릭하여 상세 흐름 확인"
echo ""
echo -e "${BLUE}로그에서 TraceId 확인:${NC}"
echo "docker logs service-auction 2>&1 | grep -E '\\[service-auction,[a-f0-9]{16},[a-f0-9]{16}\\]' | tail -5"
echo ""

