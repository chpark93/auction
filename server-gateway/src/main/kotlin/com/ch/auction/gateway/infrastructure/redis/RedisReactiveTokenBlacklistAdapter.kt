package com.ch.auction.gateway.infrastructure.redis

import com.ch.auction.gateway.domain.repository.ReactiveTokenBlacklistRepository
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class RedisReactiveTokenBlacklistAdapter(
    private val redisTemplate: ReactiveStringRedisTemplate
) : ReactiveTokenBlacklistRepository {

    override fun add(accessToken: String, ttl: Long): Mono<Boolean> {
        val key = "bl:$accessToken"
        return redisTemplate.opsForValue()
            .set(key, "logout", Duration.ofMillis(ttl))
    }

    override fun exists(accessToken: String): Mono<Boolean> {
        val key = "bl:$accessToken"
        return redisTemplate.hasKey(key)
    }
}

