package com.ch.auction.payment.interfaces.api

import com.ch.auction.common.ApiResponse
import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import com.ch.auction.payment.application.service.DeliveryService
import com.ch.auction.payment.infrastructure.client.UserClient
import com.ch.auction.payment.interfaces.api.dto.delivery.ShippingRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/seller/orders")
class SellerDeliveryController(
    private val deliveryService: DeliveryService,
    private val userClient: UserClient,
) {

    @PostMapping("/{orderId}/shipping")
    fun startShipping(
        @AuthenticationPrincipal email: String,
        @PathVariable orderId: Long,
        @RequestBody request: ShippingRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        val userResponse = userClient.getUserByEmail(
            email = email
        ).data ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        deliveryService.startShipping(
            sellerId = userResponse.userId,
            orderId = orderId,
            request = request
        )

        return ResponseEntity.ok(ApiResponse.ok())
    }
}

