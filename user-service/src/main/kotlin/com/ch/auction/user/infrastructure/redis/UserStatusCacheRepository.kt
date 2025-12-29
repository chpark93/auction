package com.ch.auction.user.infrastructure.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class UserStatusCacheRepository(
    private val redisTemplate: RedisTemplate<String, String>
) {
    companion object {
        private const val KEY_PREFIX = "user:status:"
        private val TTL = Duration.ofHours(24) // 24시간 캐시
    }

    /**
     * 사용자 상태 저장
     */
    fun saveUserStatus(
        userId: Long,
        status: String
    ) {
        val key = "$KEY_PREFIX$userId"
        redisTemplate.opsForValue().set(key, status, TTL)
    }

    /**
     * 사용자 상태 조회
     */
    fun getUserStatus(
        userId: Long
    ): String? {
        val key = "$KEY_PREFIX$userId"
        return redisTemplate.opsForValue().get(key)
    }

    /**
     * 사용자 상태 삭제
     */
    fun deleteUserStatus(
        userId: Long
    ) {
        val key = "$KEY_PREFIX$userId"
        redisTemplate.delete(key)
    }
}

