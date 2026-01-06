package com.ch.auction.gateway.security

import com.ch.auction.gateway.security.jwt.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

@Component
class JwtValidator(
    private val jwtProperties: JwtProperties
) {
    private val secretKey: SecretKey =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret))

    fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
