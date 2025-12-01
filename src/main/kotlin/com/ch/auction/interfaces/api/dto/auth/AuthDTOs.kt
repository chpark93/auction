package com.ch.auction.interfaces.api.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

object AuthDTOs {
    data class SignUpRequest(
        @field:NotBlank
        @field:Email
        val email: String,

        @field:NotBlank
        @field:Size(min = 8, max = 20)
        val password: String,

        @field:NotBlank
        val nickname: String,

        @field:NotBlank
        val name: String,

        @field:NotBlank
        val phoneNumber: String
    )

    data class LoginRequest(
        @field:NotBlank
        @field:Email
        val email: String,

        @field:NotBlank
        val password: String
    )

    data class LoginResponse(
        val accessToken: String,
        val refreshToken: String
    )
    
    data class ReissueRequest(
        @field:NotBlank
        val refreshToken: String
    )
}
