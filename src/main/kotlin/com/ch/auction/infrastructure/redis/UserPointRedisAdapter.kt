package com.ch.auction.infrastructure.redis

import com.ch.auction.domain.repository.UserPointInfo
import com.ch.auction.domain.repository.UserPointRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class UserPointRedisAdapter(
    private val redisTemplate: StringRedisTemplate
) : UserPointRepository {

    override fun chargePoint(
        userId: Long,
        amount: Long
    ): Long {
        val key = "user:$userId:point"
        return redisTemplate.opsForValue().increment(key, amount) ?: 0L
    }

    override fun getPoint(
        userId: Long
    ): UserPointInfo {
        val pointKey = "user:$userId:point"
        val lockedPointKey = "user:$userId:locked_point"
        
        val totalPointFromRedis = redisTemplate.opsForValue().get(pointKey)
        val lockedPointFromRedis = redisTemplate.opsForValue().get(lockedPointKey)
        
        val totalPoint = totalPointFromRedis?.toLongOrNull() ?: 0L
        val lockedPoint = lockedPointFromRedis?.toLongOrNull() ?: 0L
        
        return UserPointInfo(
            totalPoint = totalPoint,
            lockedPoint= lockedPoint
        )
    }
}

