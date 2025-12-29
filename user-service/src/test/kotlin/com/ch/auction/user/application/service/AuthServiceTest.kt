package com.ch.auction.user.application.service

import com.ch.auction.common.ErrorCode
import com.ch.auction.common.dto.AuthDTOs
import com.ch.auction.common.enums.UserRole
import com.ch.auction.common.enums.UserStatus
import com.ch.auction.domain.auth.AuthTokenClaims
import com.ch.auction.domain.auth.port.AuthTokenIssuer
import com.ch.auction.domain.auth.port.AuthTokenParser
import com.ch.auction.domain.repository.RefreshTokenRepository
import com.ch.auction.domain.repository.TokenBlacklistRepository
import com.ch.auction.exception.BusinessException
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class AuthServiceTest {

    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val authTokenIssuer: AuthTokenIssuer = mockk()
    private val authTokenParser: AuthTokenParser = mockk()
    private val refreshTokenRepository: RefreshTokenRepository = mockk()
    private val tokenBlacklistRepository: TokenBlacklistRepository = mockk()
    private val userStatusCacheRepository: com.ch.auction.user.infrastructure.redis.UserStatusCacheRepository = mockk(relaxed = true)

    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        authService = AuthService(
            userRepository = userRepository,
            passwordEncoder = passwordEncoder,
            authTokenIssuer = authTokenIssuer,
            authTokenParser = authTokenParser,
            refreshTokenRepository = refreshTokenRepository,
            tokenBlacklistRepository = tokenBlacklistRepository,
            userStatusCacheRepository = userStatusCacheRepository
        )
    }

    @Test
    fun signup_success() {
        // given
        val request = AuthDTOs.SignUpRequest(
            email = "test@test.com",
            password = "password123",
            nickname = "tester",
            name = "Test User",
            phoneNumber = "010-1234-5678",
            role = UserRole.ROLE_USER
        )

        every {
            userRepository.existsByEmail(
                email = request.email
            )
        } returns false
        every {
            passwordEncoder.encode(request.password)
        } returns "encrypted_password"
        every {
            userRepository.save(any())
        } returnsArgument 0

        // when
        authService.signUp(request)

        // then
        verify(exactly = 1) { passwordEncoder.encode(request.password) }
        verify(exactly = 1) { 
            userRepository.save(
                match { user ->
                    user.email == request.email &&
                    user.nickname == request.nickname
                }
            )
        }
    }

    @Test
    fun signup_duplicated_email() {
        // given
        val request = AuthDTOs.SignUpRequest(
            email = "duplicate@test.com",
            password = "password123",
            nickname = "tester",
            name = "Test User",
            phoneNumber = "010-1234-5678",
            role = UserRole.ROLE_USER
        )

        every {
            userRepository.existsByEmail(request.email)
        } returns true

        // when & then
        val exception = assertThrows<BusinessException> {
            authService.signUp(
                request = request
            )
        }
        assertEquals(ErrorCode.EMAIL_DUPLICATED, exception.errorCode)
        verify(exactly = 0) {
            userRepository.save(any())
        }
    }

    @Test
    fun login_success() {
        // given
        val request = AuthDTOs.LoginRequest(
            email = "test@test.com",
            password = "password123"
        )

        val user = mockk<User>()
        every {
            user.id
        } returns 1L
        every {
            user.email
        } returns request.email
        every {
            user.role
        } returns UserRole.ROLE_USER
        every {
            user.status
        } returns UserStatus.ACTIVE
        every {
            user.checkPassword(request.password, passwordEncoder)
        } returns true

        every {
            userRepository.findByEmail(
                email = request.email
            )
        } returns Optional.of(user)
        every {
            authTokenIssuer.issueAccessToken(
                subject = any(),
                userId = any(),
                roles = any()
            )
        } returns "access_token"
        every {
            authTokenIssuer.issueRefreshToken(
                subject = any(),
                userId = any(),
                roles = any()
            )
        } returns "refresh_token"
        every {
            refreshTokenRepository.save(
                email = any(),
                refreshToken = any()
            )
        } just Runs
        every {
            userStatusCacheRepository.saveUserStatus(
                userId = any(),
                status = any()
            )
        } just Runs

        // when
        val response = authService.login(
            request = request
        )

        // then
        assertEquals("access_token", response.accessToken)
        assertEquals("refresh_token", response.refreshToken)
        verify(exactly = 1) {
            refreshTokenRepository.save(
                email = request.email,
                refreshToken = "refresh_token"
            )
        }
    }

    @Test
    fun login_invalid_password() {
        // given
        val request = AuthDTOs.LoginRequest(
            email = "test@test.com",
            password = "wrong_password"
        )

        val user = mockk<User>()
        every {
            user.checkPassword(request.password, passwordEncoder)
        } returns false

        every {
            userRepository.findByEmail(
                email = request.email
            ) } returns Optional.of(user)

        // when & then
        val exception = assertThrows<BusinessException> {
            authService.login(
                request = request
            )
        }
        assertEquals(ErrorCode.PASSWORD_MISMATCH, exception.errorCode)
        verify(exactly = 0) {
            authTokenIssuer.issueAccessToken(
                subject = any(),
                userId = any(),
                roles = any()
            )
        }
    }

    @Test
    fun reissue_invalid_request_token_redis_token() {
        // given
        val refreshToken = "request_refresh_token"
        val savedToken = "different_refresh_token"
        val email = "test@test.com"

        val claims = mockk<AuthTokenClaims>()
        every { claims.subject } returns email

        every { authTokenParser.parse(refreshToken) } returns claims
        every { refreshTokenRepository.get(email) } returns savedToken
        every { refreshTokenRepository.delete(email) } just Runs

        // when & then
        val exception = assertThrows<BusinessException> {
            authService.reissue(refreshToken)
        }
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.errorCode)
        verify(exactly = 1) { refreshTokenRepository.delete(email) }
    }

    @Test
    fun reissue_success() {
        // given
        val refreshToken = "valid_refresh_token"
        val email = "test@test.com"
        val userId = 1L

        val claims = mockk<AuthTokenClaims>()
        every {
            claims.subject
        } returns email

        val user = mockk<User>()
        every {
            user.id
        } returns userId
        every {
            user.email
        } returns email
        every {
            user.role
        } returns UserRole.ROLE_USER
        every {
            user.status
        } returns UserStatus.ACTIVE

        every {
            authTokenParser.parse(
                token = refreshToken
            )
        } returns claims
        every {
            refreshTokenRepository.get(
                email = email
            )
        } returns refreshToken
        every {
            userRepository.findByEmail(
                email = email
            )
        } returns Optional.of(user)
        every {
            authTokenIssuer.issueAccessToken(
                subject = any(),
                userId = any(),
                roles = any()
            )
        } returns "new_access_token"
        every {
            authTokenIssuer.issueRefreshToken(
                subject = any(),
                userId = any(),
                roles = any()
            )
        } returns "new_refresh_token"
        every {
            refreshTokenRepository.save(
                email = any(),
                refreshToken = any()
            )
        } just Runs
        every {
            userStatusCacheRepository.saveUserStatus(
                userId = any(),
                status = any()
            )
        } just Runs

        // when
        val response = authService.reissue(refreshToken)

        // then
        assertEquals("new_access_token", response.accessToken)
        assertEquals("new_refresh_token", response.refreshToken)
        verify(exactly = 1) {
            refreshTokenRepository.save(
                email = email,
                refreshToken = "new_refresh_token"
            )
        }
    }
}

