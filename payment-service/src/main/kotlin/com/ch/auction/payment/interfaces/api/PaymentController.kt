package com.ch.auction.payment.interfaces.api

import com.ch.auction.common.ApiResponse
import com.ch.auction.payment.application.service.PaymentService
import com.ch.auction.payment.interfaces.api.dto.payment.PaymentRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val paymentService: PaymentService
) {

    @PostMapping("/charge")
    fun chargePoint(
        @AuthenticationPrincipal email: String,
        @RequestBody request: PaymentRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        paymentService.chargePoint(
            email = email,
            request = request
        )

        return ResponseEntity.ok(ApiResponse.ok())
    }
}

