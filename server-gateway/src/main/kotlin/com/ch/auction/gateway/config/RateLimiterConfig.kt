package com.ch.auction.gateway.config

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import reactor.core.publisher.Mono

/**
 * Rate Limiter KeyResolver 설정
 * 분산 환경에서 Redis를 사용하여 요청 제한을 처리
 */
@Configuration
class RateLimiterConfig {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 사용자 ID 기반 KeyResolver (Primary)
     * JWT에서 추출한 X-User-Id 헤더를 사용하여 사용자별로 Rate Limit 적용
     */
    @Bean
    @Primary
    fun userKeyResolver(): KeyResolver {
        return KeyResolver { exchange ->
            val userId = exchange.request.headers.getFirst("X-User-Id")
            
            if (userId != null) {
                logger.debug("Rate limit key: user:$userId")
                Mono.just("user:$userId")
            } else {
                // 로그인하지 않은 경우 IP 기반
                val remoteAddr = exchange.request.remoteAddress?.address?.hostAddress
                logger.debug("Rate limit key (no user): ip:${remoteAddr ?: "unknown"}")
                Mono.just("ip:${remoteAddr ?: "unknown"}")
            }
        }
    }

    /**
     * IP 기반 KeyResolver
     * 클라이언트 IP 주소를 기반으로 Rate Limit 적용
     */
    @Bean
    fun ipKeyResolver(): KeyResolver {
        return KeyResolver { exchange ->
            val remoteAddr = exchange.request.remoteAddress?.address?.hostAddress
            logger.debug("Rate limit key (IP only): ip:${remoteAddr ?: "unknown"}")
            Mono.just("ip:${remoteAddr ?: "unknown"}")
        }
    }

    /**
     * API Path 기반 KeyResolver
     * API 경로별로 Rate Limit 적용
     */
    @Bean
    fun pathKeyResolver(): KeyResolver {
        return KeyResolver { exchange ->
            val path = exchange.request.path.value()
            val userId = exchange.request.headers.getFirst("X-User-Id")
            
            val key = if (userId != null) {
                "user:$userId:path:$path"
            } else {
                val remoteAddr = exchange.request.remoteAddress?.address?.hostAddress
                "ip:${remoteAddr ?: "unknown"}:path:$path"
            }
            
            logger.debug("Rate limit key (path): $key")
            Mono.just(key)
        }
    }

    /**
     * 조합 KeyResolver (사용자 + IP)
     * 보안을 강화하기 위해 사용자 ID와 IP를 조합
     */
    @Bean
    fun compositeKeyResolver(): KeyResolver {
        return KeyResolver { exchange ->
            val userId = exchange.request.headers.getFirst("X-User-Id")
            val remoteAddr = exchange.request.remoteAddress?.address?.hostAddress
            
            val key = if (userId != null) {
                "user:$userId:ip:${remoteAddr ?: "unknown"}"
            } else {
                "ip:${remoteAddr ?: "unknown"}"
            }
            
            logger.debug("Rate limit key (composite): $key")
            Mono.just(key)
        }
    }
}

