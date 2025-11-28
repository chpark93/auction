-- KEYS[1]: auction info hash key
-- ARGV[1]: bid amount
-- ARGV[2]: user id
-- ARGV[3]: request time
-- ARGV[4]: min increment (최소 호가 단위)
-- ARGV[5]: max limit (자동 입찰 한도액, 0이면 일반 입찰)

local auctionInfo = redis.call('HMGET', KEYS[1], 'currentPrice', 'endTime', 'lastBidderId', 'sellerId', 'autoBidderId', 'autoMaxAmount')

if not auctionInfo or not auctionInfo[1] or not auctionInfo[2] then
    return "-1" -- Not Found
end

local currentPrice = tonumber(auctionInfo[1])
local endTime = tonumber(auctionInfo[2])
local lastBidderId = auctionInfo[3]
local sellerId = auctionInfo[4]
local currentAutoBidderId = auctionInfo[5]
local currentAutoMaxAmount = tonumber(auctionInfo[6] or "0")

local newAmount = tonumber(ARGV[1])
local requestTime = tonumber(ARGV[3])
local minIncrement = tonumber(ARGV[4] or "1000")
local myMaxLimit = tonumber(ARGV[5] or "0")

if not currentPrice or not endTime or not newAmount or not requestTime then
    return "-1"
end

-- 종료 시간 체크
if endTime < requestTime then
    return "-2" -- Ended
end

-- 판매자 입찰 금지
if sellerId and sellerId == ARGV[2] then
    return "-3" -- Self Bidding
end

-- 연속 입찰 금지 (Optional)
if lastBidderId and lastBidderId == ARGV[2] then
    return "-3"
end

-- 가격 체크
if newAmount <= currentPrice then
    return "0" -- Too Low
end

-- 자동 입찰 한도액 유효성 체크
if myMaxLimit > 0 and myMaxLimit < newAmount then
    return "0" -- Max limit must be >= bid amount
end

-- 기존 자동 입찰자와 경쟁
if currentAutoBidderId and currentAutoBidderId ~= ARGV[2] then
    if newAmount <= currentAutoMaxAmount then
        -- 방어 성공: 자동 입찰자 승리
        local defendPrice = newAmount + minIncrement
        if defendPrice > currentAutoMaxAmount then
            defendPrice = currentAutoMaxAmount
        end
        
        -- 현재가만 갱신 (입찰자는 유지)
        if defendPrice > currentPrice then
            redis.call('HSET', KEYS[1], 'currentPrice', defendPrice)
        end
        
        -- 새 입찰자는 실패 (방어됨)
        return "-5" -- Outbidded
    else
        -- 자동 입찰자 패배 -> 환불
        local autoBidderLockedKey = "user:" .. currentAutoBidderId .. ":locked_point"
        redis.call('DECRBY', autoBidderLockedKey, currentAutoMaxAmount)
        
        -- 정보 삭제
        redis.call('HDEL', KEYS[1], 'autoBidderId', 'autoMaxAmount')
    end
else
    -- 기존 일반 입찰자 환불
    if lastBidderId and lastBidderId ~= ARGV[2] then
         local lastBidderLockedKey = "user:" .. lastBidderId .. ":locked_point"
         redis.call('DECRBY', lastBidderLockedKey, currentPrice)
    end
end

-- 포인트 검증
local requiredPoint = newAmount
if myMaxLimit > 0 then
    requiredPoint = myMaxLimit
end

local userPointKey = "user:" .. ARGV[2] .. ":point"
local userLockedKey = "user:" .. ARGV[2] .. ":locked_point"
local totalPoint = tonumber(redis.call('GET', userPointKey) or "0")
local lockedPoint = tonumber(redis.call('GET', userLockedKey) or "0")
local availablePoint = totalPoint - lockedPoint

if availablePoint < requiredPoint then
    return "-4" -- Not Enough Point
end

-- 잠금
redis.call('INCRBY', userLockedKey, requiredPoint)

-- 갱신
redis.call('HSET', KEYS[1], 'currentPrice', newAmount)
redis.call('HSET', KEYS[1], 'lastBidderId', ARGV[2])

if myMaxLimit > 0 then
    redis.call('HSET', KEYS[1], 'autoBidderId', ARGV[2])
    redis.call('HSET', KEYS[1], 'autoMaxAmount', myMaxLimit)
end

local sequence = redis.call('HINCRBY', KEYS[1], 'bidSequence', 1)
local time = redis.call('TIME')
local timestamp = tonumber(time[1]) * 1000 + math.floor(tonumber(time[2]) / 1000)

return timestamp .. ":" .. sequence
