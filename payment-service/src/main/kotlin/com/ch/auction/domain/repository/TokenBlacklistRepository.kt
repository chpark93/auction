package com.ch.auction.domain.repository

interface TokenBlacklistRepository {
    fun add(
        accessToken: String,
        ttl: Long
    )

    fun exists(
        accessToken: String
    ): Boolean
}

