package com.ch.auction.interfaces.api

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.application.service.DeliveryService
import com.ch.auction.infrastructure.persistence.UserRepository
import com.ch.auction.interfaces.api.dto.delivery.ShippingRequest
import com.ch.auction.interfaces.common.ApiResponse
import com.ch.auction.interfaces.common.ErrorCode
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/seller/orders")
class SellerDeliveryController(
    private val deliveryService: DeliveryService,
    private val userRepository: UserRepository
) {

    @PostMapping("/{orderId}/shipping")
    fun startShipping(
        @AuthenticationPrincipal email: String,
        @PathVariable orderId: Long,
        @RequestBody request: ShippingRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        val user = userRepository.findByEmail(
            email = email
        ).orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        deliveryService.startShipping(
            sellerId = user.id!!,
            orderId = orderId,
            request = request
        )

        return ResponseEntity.ok(ApiResponse.ok())
    }
}

