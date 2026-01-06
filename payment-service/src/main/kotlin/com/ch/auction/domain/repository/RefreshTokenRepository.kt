package com.ch.auction.domain.repository

interface RefreshTokenRepository {
    fun save(
        email: String,
        refreshToken: String
    )

    fun get(
        email: String
    ): String?

    fun delete(
        email: String
    )
}

