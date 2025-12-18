package com.ch.auction.gateway.filter

import com.ch.auction.gateway.common.ApiResponse
import com.ch.auction.gateway.common.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Rate Limit 초과 시 커스텀 응답을 반환하는 Global Filter
 * Spring Cloud Gateway의 RequestRateLimiter가 429 응답을 반환할 때
 * 표준화된 ApiResponse 형식으로 변환
 */
@Component
class RateLimitExceededFilter(
    private val objectMapper: ObjectMapper
) : GlobalFilter, Ordered {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain
    ): Mono<Void> {
        return chain.filter(exchange).then(
            Mono.defer {
                val response = exchange.response
                
                // 429 Too Many Requests 응답 체크
                if (response.statusCode == HttpStatus.TOO_MANY_REQUESTS) {
                    logger.warn(
                        "Rate limit exceeded for request: {} {} from {}",
                        exchange.request.method,
                        exchange.request.path,
                        exchange.request.remoteAddress?.address?.hostAddress ?: "unknown"
                    )
                    
                    // 커스텀 응답 생성
                    val apiResponse = ApiResponse.fail(ErrorCode.RATE_LIMIT_EXCEEDED)
                    val bytes = objectMapper.writeValueAsBytes(apiResponse)
                    
                    // 응답 헤더 설정
                    response.headers.contentType = MediaType.APPLICATION_JSON
                    
                    // Retry-After 헤더 추가 (1초 후 재시도 권장)
                    response.headers.add("Retry-After", "1")
                    
                    // Rate Limit 정보 헤더 추가
                    response.headers.add("X-RateLimit-Remaining", "0")
                    response.headers.add("X-RateLimit-Reset", System.currentTimeMillis().toString())
                    
                    val buffer: DataBuffer = response.bufferFactory().wrap(bytes)
                    return@defer response.writeWith(Mono.just(buffer))
                }
                
                Mono.empty()
            }
        )
    }

    /**
     * 필터 순서를 가장 마지막으로 설정
     * 다른 필터들이 모두 실행된 후에 Rate Limit 체크
     */
    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE
}

