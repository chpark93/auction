package com.ch.auction.payment.infrastructure.redis

import com.ch.auction.payment.domain.UserPointInfo
import com.ch.auction.payment.domain.UserPointRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class UserPointRedisAdapter(
    private val redisTemplate: StringRedisTemplate
) : UserPointRepository {

    companion object {
        private const val USER_POINT_PREFIX = "user"
        private const val POINT_KEY = "point"
        private const val LOCKED_POINT = "locked_point"
    }

    override fun chargePoint(
        userId: Long,
        amount: Long
    ): Long {
        val key = "$USER_POINT_PREFIX:$userId:$POINT_KEY"
        return redisTemplate.opsForValue().increment(key, amount) ?: 0L
    }

    override fun usePoint(
        userId: Long,
        amount: Long
    ) {
        val pointKey = "$USER_POINT_PREFIX:$userId:$POINT_KEY"
        val lockedPointKey = "$USER_POINT_PREFIX:$userId:$LOCKED_POINT"

        redisTemplate.opsForValue().decrement(pointKey, amount)
        redisTemplate.opsForValue().decrement(lockedPointKey, amount)
    }

    override fun getPoint(
        userId: Long
    ): UserPointInfo {
        val pointKey = "$USER_POINT_PREFIX:$userId:$POINT_KEY"
        val lockedPointKey = "$USER_POINT_PREFIX:$userId:$LOCKED_POINT"
        
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
