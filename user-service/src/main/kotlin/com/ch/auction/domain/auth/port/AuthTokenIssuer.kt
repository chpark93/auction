package com.ch.auction.domain.auth.port

interface AuthTokenIssuer {
    fun issueAccessToken(
        subject: String,
        userId: Long,
        roles: List<String>
    ): String

    fun issueRefreshToken(
        subject: String,
        userId: Long,
        roles: List<String>
    ): String
}
