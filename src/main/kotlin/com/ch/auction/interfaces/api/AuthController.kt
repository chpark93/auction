package com.ch.auction.interfaces.api

import com.ch.auction.application.service.AuthService
import com.ch.auction.interfaces.api.dto.auth.AuthDTOs
import com.ch.auction.interfaces.common.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody request: AuthDTOs.SignUpRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        authService.signUp(
            request = request
        )

        return ResponseEntity.ok(ApiResponse.ok())
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: AuthDTOs.LoginRequest
    ): ResponseEntity<ApiResponse<AuthDTOs.LoginResponse>> {
        val response = authService.login(
            request = request
        )

        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<ApiResponse<Unit>> {
        val accessToken = if (token.startsWith("Bearer ")) token.substring(7) else token
        authService.logout(
            accessToken = accessToken
        )

        return ResponseEntity.ok(ApiResponse.ok())
    }

    @PostMapping("/reissue")
    fun reissue(
        @Valid @RequestBody request: AuthDTOs.ReissueRequest
    ): ResponseEntity<ApiResponse<AuthDTOs.LoginResponse>> {
        val response = authService.reissue(
            refreshToken = request.refreshToken
        )

        return ResponseEntity.ok(ApiResponse.ok(response))
    }
}
