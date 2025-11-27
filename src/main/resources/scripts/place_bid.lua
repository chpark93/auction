-- KEYS[1]: auction info hash key (e.g., "auction:1:info")
-- ARGV[1]: bid amount
-- ARGV[2]: user id

local currentPrice = tonumber(redis.call('HGET', KEYS[1], 'currentPrice'))
local newAmount = tonumber(ARGV[1])
local userId = ARGV[2]

-- 현재가가 없으면(아직 입찰 없음) 0으로 취급 (또는 초기 세팅 필요)
if currentPrice == nil then
    currentPrice = 0
end

if newAmount > currentPrice then
    redis.call('HSET', KEYS[1], 'currentPrice', newAmount)
    redis.call('HSET', KEYS[1], 'highestBidder', userId)
    return 1 -- 성공
else
    return 0 -- 실패 (더 높은 가격 존재)
end

