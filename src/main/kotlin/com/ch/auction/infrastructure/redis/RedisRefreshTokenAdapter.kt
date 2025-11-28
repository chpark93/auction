package com.ch.auction.infrastructure.redis

import com.ch.auction.domain.repository.RefreshTokenRepository
import com.ch.auction.infrastructure.security.jwt.JwtProperties
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
            key(
                email = email
            ),
            refreshToken,
            Duration.ofMillis(jwtProperties.refreshTokenTtl)
        )
    }

    override fun get(
        email: String
    ): String? {
        return redisTemplate.opsForValue().get(
            key(
                email = email
            )
        )
    }

    override fun delete(
        email: String
    ) {
        redisTemplate.delete(
            key(
                email = email
            )
        )
    }

    private fun key(
        email: String
    ) = "$KEY_PREFIX:$email"
}
