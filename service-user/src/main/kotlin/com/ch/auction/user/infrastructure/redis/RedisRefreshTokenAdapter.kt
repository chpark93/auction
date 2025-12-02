package com.ch.auction.user.infrastructure.redis

import com.ch.auction.common.security.jwt.JwtProperties
import com.ch.auction.domain.repository.RefreshTokenRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisRefreshTokenAdapter(
    private val redisTemplate: StringRedisTemplate,
    private val jwtProperties: JwtProperties
) : RefreshTokenRepository {

    companion object {
        private const val KEY_PREFIX = "auth"
    }

    override fun save(
        email: String,
        refreshToken: String
    ) {
        redisTemplate.opsForValue().set(
            key(email),
            refreshToken,
            Duration.ofMillis(jwtProperties.refreshTokenTtl)
        )
    }

    override fun get(
        email: String
    ): String? {
        return redisTemplate.opsForValue().get(key(email))
    }

    override fun delete(
        email: String
    ) {
        redisTemplate.delete(key(email))
    }

    private fun key(
        email: String
    ) = "$KEY_PREFIX:$email"
}
