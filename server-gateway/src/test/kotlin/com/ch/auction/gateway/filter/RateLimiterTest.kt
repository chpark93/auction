package com.ch.auction.gateway.filter

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Disabled("Redis 연결이 필요한 통합 테스트. 필요 시 활성화")
@DisplayName("Rate Limiter 통합 테스트")
class RateLimiterTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    @DisplayName("입찰 요청 Rate Limit 테스트 - 제한 초과 시 429 반환")
    fun return_429_when_bid_rate_limit_exceeded() {
        // Given
        val userId = "test-user-1"
        
        // When
        repeat(11) { index ->
            val response = webTestClient
                .post()
                .uri("/api/v1/auctions/1/bid")
                .header("X-User-Id", userId)
                .header("Authorization", "Bearer test-token")
                .bodyValue(mapOf("amount" to 10000))
                .exchange()
            
            // Then
            if (index >= 10) {
                // 11번째부터는 Rate Limit 초과 (burstCapacity: 10)
                response
                    .expectStatus().isEqualTo(429)
                    .expectHeader().exists("Retry-After")
                    .expectHeader().valueEquals("X-RateLimit-Remaining", "0")
                    .expectBody()
                    .jsonPath("$.success").isEqualTo(false)
                    .jsonPath("$.error.code").isEqualTo("R001")
                    .jsonPath("$.error.message").exists()
            }
            // 처음 10개는 429가 아니면 성공 (인증 실패 등 다른 에러 가능)
        }
    }

    @Test
    @DisplayName("검색 요청 Rate Limit 테스트 - IP 기반 제한")
    fun search_rate_limit_by_IP_address() {
        // Given
        // 로그인하지 않은 사용자 (IP 기반 제한)
        
        // When & Then
        // 짧은 시간 내에 여러 검색 요청 (burstCapacity: 50)
        repeat(51) { index ->
            val response = webTestClient
                .get()
                .uri("/api/v1/search/auctions?keyword=test")
                .exchange()
            
            if (index >= 50) {
                // 51번째 요청은 429 반환
                response
                    .expectStatus().isEqualTo(429)
                    .expectHeader().exists("Retry-After")
            }
            // 처음 50개는 429가 아니면 성공
        }
    }

    @Test
    @DisplayName("다른 사용자는 독립적인 Rate Limit 적용")
    fun apply_independent_rate_limits_for_different_users() {
        // Given
        // 두 명의 다른 사용자
        val user1 = "test-user-1"
        val user2 = "test-user-2"
        
        // When & Then
        // User 1이 Rate Limit에 도달하도록 요청
        repeat(11) {
            webTestClient
                .post()
                .uri("/api/v1/auctions/1/bid")
                .header("X-User-Id", user1)
                .header("Authorization", "Bearer test-token-1")
                .bodyValue(mapOf("amount" to 10000))
                .exchange()
        }
        
        // User 1의 추가 요청은 실패해야 함
        webTestClient
            .post()
            .uri("/api/v1/auctions/1/bid")
            .header("X-User-Id", user1)
            .header("Authorization", "Bearer test-token-1")
            .bodyValue(mapOf("amount" to 10000))
            .exchange()
            .expectStatus().isEqualTo(429)
        
        // User 2는 여전히 요청 가능 (독립적인 Rate Limit)
        webTestClient
            .post()
            .uri("/api/v1/auctions/1/bid")
            .header("X-User-Id", user2)
            .header("Authorization", "Bearer test-token-2")
            .bodyValue(mapOf("amount" to 10000))
            .exchange()
            // 429가 아니면 성공 (인증 실패 등 다른 에러 가능)
    }

    @Test
    @DisplayName("Rate Limit 복구 테스트 - 시간 경과 후 정상 처리")
    fun recover_after_rate_limit_passes() {
        // Given
        // Rate Limit에 도달한 사용자
        val userId = "test-user-recovery"
        
        // burstCapacity까지 요청 (10개)
        repeat(10) {
            webTestClient
                .post()
                .uri("/api/v1/auctions/1/bid")
                .header("X-User-Id", userId)
                .header("Authorization", "Bearer test-token")
                .bodyValue(mapOf("amount" to 10000))
                .exchange()
        }
        
        // 다음 요청은 실패해야 함
        webTestClient
            .post()
            .uri("/api/v1/auctions/1/bid")
            .header("X-User-Id", userId)
            .header("Authorization", "Bearer test-token")
            .bodyValue(mapOf("amount" to 10000))
            .exchange()
            .expectStatus().isEqualTo(429)
        
        // When
        // 충분한 시간 대기 (1초 = replenishRate에 따라 5개 토큰 충전)
        Thread.sleep(1200)
        
        // Then
        // 재요청 가능 (429가 아니면 성공)
        val response = webTestClient
            .post()
            .uri("/api/v1/auctions/1/bid")
            .header("X-User-Id", userId)
            .header("Authorization", "Bearer test-token")
            .bodyValue(mapOf("amount" to 10000))
            .exchange()
        
        // 429가 아닌 다른 상태 코드 확인 (성공 또는 인증 실패 등)
        response.expectStatus().value { status ->
            assert(status != 429) { "Rate limit should be recovered after waiting" }
        }
    }
}

