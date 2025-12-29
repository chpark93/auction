package com.ch.auction.auction.infrastructure.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class UserStatusCacheRepository(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val USER_STATUS_KEY_PREFIX = "user:status:"
        private const val CACHE_TTL_SECONDS = 3600L
    }

    fun getUserStatus(
        userId: Long
    ): String? {
        return redisTemplate.opsForValue().get("$USER_STATUS_KEY_PREFIX$userId")
    }

    fun saveUserStatus(
        userId: Long,
        status: String
    ) {
        redisTemplate.opsForValue().set(
            "$USER_STATUS_KEY_PREFIX$userId",
            status,
            CACHE_TTL_SECONDS,
            TimeUnit.SECONDS
        )
    }

    fun deleteUserStatus(
        userId: Long
    ) {
        redisTemplate.delete("$USER_STATUS_KEY_PREFIX$userId")
    }
}

