-- KEYS[1]: auction info hash key
-- ARGV[1]: amount, ARGV[2]: userId, ARGV[3]: requestTime

local key = KEYS[1]
local info = redis.call('HMGET', key, 'currentPrice', 'endTime', 'lastBidderId', 'sellerId')

if not info[1] or not info[2] then return "-1" end -- Not Found

local currentPrice = tonumber(info[1])
local endTime = tonumber(info[2])
local lastBidderId = info[3]
local sellerId = info[4]

local newAmount = tonumber(ARGV[1])
local userId = ARGV[2]
local requestTime = tonumber(ARGV[3])

if endTime < requestTime then return "-2" end -- Ended
if sellerId == userId then return "-3" end -- Self Bidding
if lastBidderId == userId then return "-3" end -- Consecutive Bidding

-- 가격 체크
if currentPrice > 0 and newAmount <= currentPrice then 
    return "0" -- Too Low
end

-- 포인트 검증
local userPointKey = "user:" .. userId .. ":point"
local userLockedKey = "user:" .. userId .. ":locked_point"
local totalPoint = tonumber(redis.call('GET', userPointKey) or "0")
local lockedPoint = tonumber(redis.call('GET', userLockedKey) or "0")
local requiredPoint = newAmount

if (totalPoint - lockedPoint) < requiredPoint then return "-4" end -- Not Enough Point

-- 기존 입찰자 환불
if lastBidderId and lastBidderId ~= userId then
    redis.call('DECRBY', "user:" .. lastBidderId .. ":locked_point", currentPrice)
end

-- 포인트 잠금
redis.call('INCRBY', userLockedKey, requiredPoint)

-- 상태 갱신
redis.call('HSET', key, 'currentPrice', newAmount)
redis.call('HSET', key, 'lastBidderId', userId)

-- 시퀀스 발급
local seq = redis.call('HINCRBY', key, 'bidSequence', 1)
local time = redis.call('TIME')
local timestamp = tonumber(time[1]) * 1000 + math.floor(tonumber(time[2]) / 1000)

return timestamp .. ":" .. seq
