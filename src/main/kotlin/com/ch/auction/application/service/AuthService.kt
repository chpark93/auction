package com.ch.auction.application.service

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.domain.User
import com.ch.auction.domain.auth.port.AuthTokenIssuer
import com.ch.auction.domain.auth.port.AuthTokenParser
import com.ch.auction.domain.repository.RefreshTokenRepository
import com.ch.auction.domain.repository.TokenBlacklistRepository
import com.ch.auction.infrastructure.persistence.UserRepository
import com.ch.auction.infrastructure.security.jwt.JwtProperties
import com.ch.auction.interfaces.api.dto.auth.AuthDTOs
import com.ch.auction.interfaces.common.ErrorCode
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authTokenIssuer: AuthTokenIssuer,
    private val authTokenParser: AuthTokenParser,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val tokenBlacklistRepository: TokenBlacklistRepository,
    private val jwtProperties: JwtProperties
) {
    @Transactional
    fun signUp(
        request: AuthDTOs.SignUpRequest
    ) {
        if (userRepository.existsByEmail(email = request.email)) {
            throw BusinessException(ErrorCode.EMAIL_DUPLICATED)
        }

        val user = User.create(
            email = request.email,
            rawPassword = request.password,
            passwordEncoder = passwordEncoder,
            nickname = request.nickname,
            name = request.name,
            phoneNumber = request.phoneNumber,
            point = BigDecimal.ZERO
        )

        userRepository.save(user)
    }

    @Transactional
    fun login(
        request: AuthDTOs.LoginRequest
    ): AuthDTOs.LoginResponse {
        val user = userRepository.findByEmail(
            email = request.email
        ).orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        if (!user.checkPassword(request.password, passwordEncoder)) {
            throw BusinessException(ErrorCode.PASSWORD_MISMATCH)
        }

        val roles = listOf(user.role.name)
        val accessToken = authTokenIssuer.issueAccessToken(
            subject = user.email,
            roles = roles
        )
        val refreshToken = authTokenIssuer.issueRefreshToken(
            subject = user.email,
            roles = roles
        )

        refreshTokenRepository.save(
            email = user.email,
            refreshToken = refreshToken
        )

        return AuthDTOs.LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    @Transactional
    fun logout(
        accessToken: String
    ) {
        try {
            val claims = authTokenParser.parse(
                token = accessToken
            )
            val email = claims.subject
            
            refreshTokenRepository.delete(
                email = email
            )
            
            tokenBlacklistRepository.add(
                accessToken = accessToken,
                ttl = jwtProperties.accessTokenTtl
            )

        } catch (_: Exception) {
            throw BusinessException(ErrorCode.TOKEN_INVALID)
        }
    }

    @Transactional
    fun reissue(
        refreshToken: String
    ): AuthDTOs.LoginResponse {
        val claims = try {
             authTokenParser.parse(
                 token = refreshToken
             )
        } catch (_: Exception) {
            throw BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val email = claims.subject

        val savedToken = refreshTokenRepository.get(
            email = email
        )
        if (savedToken != refreshToken) {
            refreshTokenRepository.delete(
                email = email
            )

            throw BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val user = userRepository.findByEmail(
            email = email
        ).orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val roles = listOf(user.role.name)
        val newAccessToken = authTokenIssuer.issueAccessToken(
            subject = user.email,
            roles = roles
        )
        val newRefreshToken = authTokenIssuer.issueRefreshToken(
            subject = user.email,
            roles = roles
        )

        refreshTokenRepository.save(
            email = email,
            refreshToken = newRefreshToken
        )

        return AuthDTOs.LoginResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }
}
