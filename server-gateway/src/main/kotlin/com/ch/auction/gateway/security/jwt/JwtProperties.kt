package com.ch.auction.gateway.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(
    "jwt"
)
data class JwtProperties(
    val secret: String,
    val issuer: String = "auction-service",
    val accessTokenTtl: Long = 1800000,
    val refreshTokenTtl: Long = 604800000
)
