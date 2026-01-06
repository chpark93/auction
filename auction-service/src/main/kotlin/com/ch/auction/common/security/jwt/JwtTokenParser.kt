package com.ch.auction.common.security.jwt

import com.ch.auction.exception.BusinessException
import com.ch.auction.domain.auth.AuthTokenClaims
import com.ch.auction.domain.auth.port.AuthTokenParser
import com.ch.auction.common.ErrorCode
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

@Component
class JwtTokenParser(
    private val jwtProperties: JwtProperties
) : AuthTokenParser {

    private val secretKey: SecretKey =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret))

    override fun parse(
        token: String
    ): AuthTokenClaims =
        try {
            val jwt = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)

            val body = jwt.payload
            val subject = body.subject ?: throw BusinessException(ErrorCode.TOKEN_INVALID)
            
            // userId 추출
            val userId = body["userId"]?.toString()?.toLongOrNull()
                ?: throw BusinessException(ErrorCode.TOKEN_INVALID)

            if (body.issuer != jwtProperties.issuer) {
                throw BusinessException(ErrorCode.TOKEN_INVALID)
            }

            val roles = (body["roles"] as? List<*>)?.map { it.toString() } ?: emptyList()
            val type = body["type"]?.toString() ?: ""

            val tokenType = runCatching {
                AuthTokenClaims.TokenType.valueOf(type)
            }.getOrElse {
                throw BusinessException(ErrorCode.TOKEN_WRONG_TYPE)
            }

            AuthTokenClaims(
                subject = subject,
                userId = userId,
                roles = roles,
                tokenType = tokenType
            )
        } catch (_: ExpiredJwtException) {
            throw BusinessException(ErrorCode.TOKEN_EXPIRED)
        } catch (_: MalformedJwtException) {
            throw BusinessException(ErrorCode.TOKEN_INVALID)
        } catch (_: SignatureException) {
            throw BusinessException(ErrorCode.TOKEN_INVALID)
        } catch (_: UnsupportedJwtException) {
            throw BusinessException(ErrorCode.TOKEN_UNSUPPORTED)
        } catch (_: IllegalArgumentException) {
            throw BusinessException(ErrorCode.TOKEN_EMPTY)
        } catch (_: JwtException) {
            throw BusinessException(ErrorCode.TOKEN_INVALID)
        }
}

