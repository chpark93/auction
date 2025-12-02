package com.ch.auction.gateway.filter

import com.ch.auction.gateway.common.ApiResponse
import com.ch.auction.gateway.common.ErrorCode
import com.ch.auction.gateway.domain.repository.ReactiveTokenBlacklistRepository
import com.ch.auction.gateway.security.JwtValidator
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthorizationHeaderFilter(
    private val jwtValidator: JwtValidator,
    private val tokenBlacklistRepository: ReactiveTokenBlacklistRepository,
    private val objectMapper: ObjectMapper
) : AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config>(Config::class.java) {

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }

    data class Config(
        val enabled: Boolean = true
    )

    override fun apply(
        config: Config
    ): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request

            if (!request.headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                return@GatewayFilter onError(
                    exchange = exchange,
                    errorCode = ErrorCode.TOKEN_NOT_FOUND
                )
            }

            val authorizationHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: ""
            if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
                return@GatewayFilter onError(
                    exchange = exchange,
                    errorCode = ErrorCode.TOKEN_INVALID
                )
            }

            val token = authorizationHeader.substring(BEARER_PREFIX.length)

            tokenBlacklistRepository.exists(
                accessToken = token
            ).flatMap { isBlacklisted ->
                if (isBlacklisted) {
                    onError(
                        exchange = exchange,
                        errorCode = ErrorCode.TOKEN_INVALID
                    )
                } else {
                    processTokenValidation(
                        exchange = exchange,
                        chain = chain,
                        token = token
                    )
                }
            }
        }
    }

    private fun processTokenValidation(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain,
        token: String
    ): Mono<Void> {
        return try {
            val claims = jwtValidator.parseClaims(
                token = token
            )

            val email = claims.subject
            val userId = claims["userId"]?.toString()
            val roles = claims["roles"] as? List<*>

            val mutatedRequest = exchange.request.mutate()
                .header("X-User-Email", email)
                .header("X-User-Id", userId)
                .header("X-User-Roles", roles?.joinToString(","))
                .build()

            chain.filter(exchange.mutate().request(mutatedRequest).build())
        } catch (e: Exception) {
            onError(exchange, ErrorCode.TOKEN_INVALID)
        }
    }

    private fun onError(
        exchange: ServerWebExchange,
        errorCode: ErrorCode
    ): Mono<Void> {
        val response = exchange.response
        response.statusCode = errorCode.status
        response.headers.contentType = MediaType.APPLICATION_JSON

        val apiResponse = ApiResponse.fail(
            errorCode = errorCode
        )
        val bytes = objectMapper.writeValueAsBytes(apiResponse)
        val buffer: DataBuffer = response.bufferFactory().wrap(bytes)

        return response.writeWith(Mono.just(buffer))
    }
}
