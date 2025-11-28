-- KEYS[1]: auction info hash key (e.g., "auction:1")
-- ARGV[1]: bid amount
-- ARGV[2]: user id
-- ARGV[3]: request time (timestamp) - 경매 종료 체크용

local auctionInfo = redis.call('HMGET', KEYS[1], 'currentPrice', 'endTime')

if not auctionInfo or not auctionInfo[1] or not auctionInfo[2] then
    return "-1" -- Not Found
end

local currentPrice = tonumber(auctionInfo[1])
local endTime = tonumber(auctionInfo[2])
local newAmount = tonumber(ARGV[1])
local requestTime = tonumber(ARGV[3])

if not currentPrice or not endTime or not newAmount or not requestTime then
    return "-1"
end

-- 종료 시간 체크
if endTime < requestTime then
    return "-2" -- Ended
end

-- 가격 체크
if newAmount <= currentPrice then
    return "0" -- Too Low
end

-- 갱신
redis.call('HSET', KEYS[1], 'currentPrice', newAmount)
redis.call('HSET', KEYS[1], 'lastBidderId', ARGV[2])

-- 시퀀스 증가 (순서 보장)
local sequence = redis.call('HINCRBY', KEYS[1], 'bidSequence', 1)

-- Redis 서버 시간 가져오기 (초, 마이크로초)
local time = redis.call('TIME')
local timestamp = tonumber(time[1]) * 1000 + math.floor(tonumber(time[2]) / 1000)

-- 성공 시 "timestamp:sequence" 형식 반환
return timestamp .. ":" .. sequence
