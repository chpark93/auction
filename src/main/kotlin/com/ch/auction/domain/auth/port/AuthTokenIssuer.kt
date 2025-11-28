package com.ch.auction.domain.auth.port

interface AuthTokenIssuer {
    fun issueAccessToken(
        subject: String,
        roles: List<String>
    ): String

    fun issueRefreshToken(
        subject: String,
        roles: List<String>
    ): String
}

