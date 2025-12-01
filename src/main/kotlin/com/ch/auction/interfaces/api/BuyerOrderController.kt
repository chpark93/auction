package com.ch.auction.interfaces.api

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.application.service.SettlementService
import com.ch.auction.infrastructure.persistence.UserRepository
import com.ch.auction.interfaces.common.ApiResponse
import com.ch.auction.interfaces.common.ErrorCode
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
    private val userRepository: UserRepository
) {

    @PostMapping("/{orderId}/confirm")
    fun confirmPurchase(
        @AuthenticationPrincipal email: String,
        @PathVariable orderId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        val user = userRepository.findByEmail(
            email = email
        ).orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        settlementService.confirmPurchase(
            buyerId = user.id!!,
            orderId = orderId
        )

        return ResponseEntity.ok(ApiResponse.ok())
    }
}

