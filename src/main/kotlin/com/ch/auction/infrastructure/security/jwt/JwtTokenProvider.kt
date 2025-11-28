package com.ch.auction.infrastructure.security.jwt

import com.ch.auction.domain.auth.AuthTokenClaims
import com.ch.auction.domain.auth.port.AuthTokenIssuer
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) : AuthTokenIssuer {

    private val secretKey: SecretKey =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret))

    override fun issueAccessToken(
        subject: String,
        roles: List<String>
    ): String = createToken(
        subject = subject,
        roles = roles,
        tokenType = AuthTokenClaims.TokenType.ACCESS_TOKEN,
        ttl = jwtProperties.accessTokenTtl
    )

    override fun issueRefreshToken(
        subject: String,
        roles: List<String>
    ): String = createToken(
        subject = subject,
        roles = roles,
        tokenType = AuthTokenClaims.TokenType.REFRESH_TOKEN,
        ttl = jwtProperties.refreshTokenTtl
    )

    private fun createToken(
        subject: String,
        roles: List<String>,
        tokenType: AuthTokenClaims.TokenType,
        ttl: Long
    ): String {
        val now = Instant.now()
        val expired = now.plusMillis(ttl)

        val claims: MutableMap<String, Any> = mutableMapOf(
            "roles" to roles,
            "type" to tokenType.name
        )

        return Jwts.builder()
            .issuer(jwtProperties.issuer)
            .subject(subject)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expired))
            .claims(claims)
            .signWith(secretKey)
            .compact()
    }
}

