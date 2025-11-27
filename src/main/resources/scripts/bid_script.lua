-- KEYS[1]: auction info hash key (e.g., "auction:1")
-- ARGV[1]: bid amount
-- ARGV[2]: user id
-- ARGV[3]: request time (timestamp)

local auctionInfo = redis.call('HMGET', KEYS[1], 'currentPrice', 'endTime')
local currentPrice = tonumber(auctionInfo[1])
local endTime = tonumber(auctionInfo[2])

-- 2. 데이터가 없으면 NOT_FOUND (-1) 반환
if currentPrice == nil or endTime == nil then
    return -1
end

local requestTime = tonumber(ARGV[3])
local newAmount = tonumber(ARGV[1])

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
