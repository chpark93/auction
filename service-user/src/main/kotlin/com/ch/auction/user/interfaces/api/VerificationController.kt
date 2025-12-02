package com.ch.auction.user.interfaces.api

import com.ch.auction.common.ApiResponse
import com.ch.auction.user.application.dto.VerificationRequest
import com.ch.auction.user.application.service.VerificationService
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