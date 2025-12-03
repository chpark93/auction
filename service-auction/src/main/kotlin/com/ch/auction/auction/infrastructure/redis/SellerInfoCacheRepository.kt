package com.ch.auction.auction.infrastructure.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class SellerInfoCacheRepository(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val SELLER_NAME_KEY_PREFIX = "seller:name:"
        private const val CACHE_TTL_SECONDS = 3600L
    }

    fun getSellerNickName(
        userId: Long
    ): String? {
        return redisTemplate.opsForValue().get("$SELLER_NAME_KEY_PREFIX$userId")
    }

    fun saveSellerNickName(
        userId: Long,
        nickname: String
    ) {
        redisTemplate.opsForValue().set(
            "$SELLER_NAME_KEY_PREFIX$userId",
            nickname,
            CACHE_TTL_SECONDS,
            TimeUnit.SECONDS
        )
    }

    fun deleteSellerName(
        userId: Long
    ) {
        redisTemplate.delete("$SELLER_NAME_KEY_PREFIX$userId")
    }
}

