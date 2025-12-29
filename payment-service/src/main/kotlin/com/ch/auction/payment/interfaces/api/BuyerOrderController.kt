package com.ch.auction.payment.interfaces.api

import com.ch.auction.common.ApiResponse
import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import com.ch.auction.payment.application.service.SettlementService
import com.ch.auction.payment.infrastructure.client.UserClient
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/buyer/orders")
class BuyerOrderController(
    private val settlementService: SettlementService,
    private val userClient: UserClient,
) {

    @PostMapping("/{orderId}/confirm")
    fun confirmPurchase(
        @AuthenticationPrincipal email: String,
        @PathVariable orderId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        val userResponse = userClient.getUserByEmail(
            email = email
        ).data ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        settlementService.confirmPurchase(
            buyerId = userResponse.userId,
            orderId = orderId
        )

        return ResponseEntity.ok(ApiResponse.ok())
    }
}

