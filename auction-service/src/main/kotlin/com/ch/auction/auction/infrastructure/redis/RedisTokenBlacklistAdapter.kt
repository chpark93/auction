package com.ch.auction.auction.infrastructure.redis

import com.ch.auction.domain.repository.TokenBlacklistRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisTokenBlacklistAdapter(
    private val redisTemplate: StringRedisTemplate
) : TokenBlacklistRepository {

    companion object {
        private const val KEY_PREFIX = "blacklist"
        private const val BLACKLIST_VALUE = "BLACK"
    }

    override fun add(
        accessToken: String,
        ttl: Long
    ) {
        redisTemplate.opsForValue().set(
            key(accessToken),
            BLACKLIST_VALUE,
            Duration.ofMillis(ttl)
        )
    }

    override fun exists(
        accessToken: String
    ): Boolean {
        return redisTemplate.hasKey(key(accessToken))
    }

    private fun key(
        token: String
    ) = "$KEY_PREFIX:$token"
}

