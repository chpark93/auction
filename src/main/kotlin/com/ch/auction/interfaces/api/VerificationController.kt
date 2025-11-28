package com.ch.auction.interfaces.api

import com.ch.auction.application.service.VerificationService
import com.ch.auction.interfaces.api.dto.user.VerificationRequest
import com.ch.auction.interfaces.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users/verification")
class VerificationController(
    private val verificationService: VerificationService
) {

    @PostMapping("/complete")
    fun completeVerification(
        @AuthenticationPrincipal email: String,
        @RequestBody request: VerificationRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        verificationService.completeVerification(
            email = email,
            request = request
        )

        return ResponseEntity.ok(
            ApiResponse.ok()
        )
    }
}

