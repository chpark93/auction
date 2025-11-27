-- KEYS[1]: auction info hash key (e.g., "auction:1")
-- ARGV[1]: bid amount
-- ARGV[2]: user id
-- ARGV[3]: request time (timestamp)

local auctionInfo = redis.call('HMGET', KEYS[1], 'currentPrice', 'endTime')

-- 데이터 존재 여부 확인 (nil 체크 강화)
if not auctionInfo or not auctionInfo[1] or not auctionInfo[2] then
    return -1
end

local currentPrice = tonumber(auctionInfo[1])
local endTime = tonumber(auctionInfo[2])
local newAmount = tonumber(ARGV[1])
local requestTime = tonumber(ARGV[3])

-- 숫자 변환 유효성 체크
if not currentPrice or not endTime or not newAmount or not requestTime then
    return -1
end

-- 3. endTime < requestTime 이면 ENDED (-2) 반환
if endTime < requestTime then
    return -2
end

-- 4. amount <= currentPrice 이면 TOO_LOW (0) 반환
if newAmount <= currentPrice then
    return 0
end

-- 5. 검증 통과 시: currentPrice와 lastBidderId를 갱신하고 SUCCESS (1) 반환
redis.call('HSET', KEYS[1], 'currentPrice', newAmount)
redis.call('HSET', KEYS[1], 'lastBidderId', ARGV[2])

return 1
