-- Redis Lua Script for Atomic Bid Processing
-- KEYS[1]: auction:{auctionId}
-- ARGV[1]: userId
-- ARGV[2]: bidAmount
-- ARGV[3]: requestTime
-- ARGV[4]: userPoint (현재 사용자 포인트)

local key = KEYS[1]
local userId = tonumber(ARGV[1])
local bidAmount = tonumber(ARGV[2])
local requestTime = tonumber(ARGV[3])
local userPoint = tonumber(ARGV[4])

-- 경매 정보 조회
local auctionData = redis.call('HGETALL', key)

if #auctionData == 0 then
    return '-1' -- AUCTION_NOT_FOUND
end

-- Hash를 테이블로 변환
local auction = {}
for i = 1, #auctionData, 2 do
    auction[auctionData[i]] = auctionData[i + 1]
end

-- 경매 상태 확인
local status = auction['status']
if status ~= 'ONGOING' then
    return '-2' -- NOT ONGOING
end

-- 현재가 확인
local currentPrice = tonumber(auction['currentPrice'])
if not currentPrice then
    return '-1' -- INVALID_AUCTION_DATA
end

-- 입찰가 검증
if bidAmount <= currentPrice then
    return '0' -- PRICE_TOO_LOW
end

-- 판매자 자기 입찰 방지
local sellerId = tonumber(auction['sellerId'])
if userId == sellerId then
    return '-3' -- SELF_BIDDING
end

-- 포인트 검증
if userPoint < bidAmount then
    return '-4' -- NOT_ENOUGH_POINT
end

-- 입찰 성공: 현재가 업데이트
redis.call('HSET', key, 'currentPrice', bidAmount)
redis.call('HSET', key, 'lastBidderId', userId)
redis.call('HSET', key, 'lastBidTime', requestTime)

-- 입찰 횟수 증가
local bidCount = tonumber(auction['bidCount']) or 0
redis.call('HINCRBY', key, 'bidCount', 1)

-- Unique 입찰자 관리 (Set 사용)
local biddersKey = key .. ':bidders'
redis.call('SADD', biddersKey, userId)
local uniqueBidderCount = redis.call('SCARD', biddersKey)
redis.call('HSET', key, 'uniqueBidders', uniqueBidderCount)

-- 입찰 시퀀스 생성 (timestamp:sequence 형식)
local sequence = redis.call('HINCRBY', key, 'bidSequence', 1)

return requestTime .. ':' .. sequence

