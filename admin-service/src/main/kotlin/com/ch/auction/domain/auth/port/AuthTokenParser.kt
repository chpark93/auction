package com.ch.auction.domain.auth.port

import com.ch.auction.domain.auth.AuthTokenClaims

interface AuthTokenParser {
    fun parse(
        token: String
    ): AuthTokenClaims
}
