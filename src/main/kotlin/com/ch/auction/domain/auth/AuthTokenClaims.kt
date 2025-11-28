package com.ch.auction.domain.auth

data class AuthTokenClaims(
    val subject: String,
    val roles: List<String>,
    val tokenType: TokenType
) {
    enum class TokenType {
        ACCESS_TOKEN,
        REFRESH_TOKEN
    }
}

