package com.ch.auction.common.security.jwt

import com.ch.auction.domain.auth.AuthTokenClaims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var jwtProperties: JwtProperties
    private lateinit var secretKey: SecretKey

    @BeforeEach
    fun setUp() {
        jwtProperties = JwtProperties(
            secret = "c2VjcmV0LWtleS1mb3ItYXVjdGlvbi1wcm9qZWN0LXRlc3QtMTIzNDU2Nzg5MA==",
            issuer = "auction-service",
            accessTokenTtl = 1800000, // 30분
            refreshTokenTtl = 604800000 // 7일
        )
        jwtTokenProvider = JwtTokenProvider(jwtProperties)
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret))
    }

    @Test
    fun issue_access_token_verify_create_and_expire() {
        // given
        val subject = "test@test.com"
        val userId = 100L
        val roles = listOf("ROLE_USER")

        // when
        val token = jwtTokenProvider.issueAccessToken(
            subject = subject,
            userId = userId,
            roles = roles
        )

        // then
        assertNotNull(token)

        val claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload

        assertEquals(subject, claims.subject)
        assertEquals(userId, claims["userId"].toString().toLong())
        assertEquals(roles, claims["roles"])
        assertEquals(AuthTokenClaims.TokenType.ACCESS_TOKEN.name, claims["type"])

        val expirationTime = claims.expiration.time
        val issuedTime = claims.issuedAt.time
        val ttl = expirationTime - issuedTime

        assertTrue(ttl >= jwtProperties.accessTokenTtl - 1000)
        assertTrue(ttl <= jwtProperties.accessTokenTtl + 1000)
    }

    @Test
    fun issue_refresh_token_verify_create_and_expire() {
        // given
        val subject = "test@test.com"
        val userId = 100L
        val roles = listOf("ROLE_USER")

        // when
        val token = jwtTokenProvider.issueRefreshToken(
            subject = subject,
            userId = userId,
            roles = roles
        )

        // then
        assertNotNull(token)

        val claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload

        assertEquals(subject, claims.subject)
        assertEquals(userId, claims["userId"].toString().toLong())
        assertEquals(roles, claims["roles"])
        assertEquals(AuthTokenClaims.TokenType.REFRESH_TOKEN.name, claims["type"])

        val expirationTime = claims.expiration.time
        val issuedTime = claims.issuedAt.time
        val ttl = expirationTime - issuedTime
        assertTrue(ttl >= jwtProperties.refreshTokenTtl - 1000)
        assertTrue(ttl <= jwtProperties.refreshTokenTtl + 1000)
    }

    @Test
    fun validate_token_success() {
        // given
        val subject = "test@test.com"
        val userId = 100L
        val roles = listOf("ROLE_USER")
        val token = jwtTokenProvider.issueAccessToken(
            subject = subject,
            userId = userId,
            roles = roles
        )

        // when & then - 파싱 시 예외가 발생하지 않으면 유효한 토큰
        val claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload

        assertEquals(subject, claims.subject)
    }

    @Test
    fun validate_token_expired_token() {
        val now = Instant.now()
        val expired = now.minusSeconds(3600)

        val expiredToken = Jwts.builder()
            .issuer(jwtProperties.issuer)
            .subject("test@test.com")
            .issuedAt(Date.from(now.minusSeconds(7200)))
            .expiration(Date.from(expired))
            .claim("userId", 100L)
            .claim("roles", listOf("ROLE_USER"))
            .claim("type", AuthTokenClaims.TokenType.ACCESS_TOKEN.name)
            .signWith(secretKey)
            .compact()

        // when & then
        assertThrows(io.jsonwebtoken.ExpiredJwtException::class.java) {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(expiredToken)
        }
    }

    @Test
    fun validate_token_tampered_token() {
        // given
        val token = jwtTokenProvider.issueAccessToken(
            subject = "test@test.com",
            userId = 100L,
            roles = listOf("ROLE_USER")
        )

        // 변조
        val tamperedToken = token.dropLast(5) + "AAAAA"

        // when & then
        assertThrows(io.jsonwebtoken.security.SignatureException::class.java) {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(tamperedToken)
        }
    }
}

