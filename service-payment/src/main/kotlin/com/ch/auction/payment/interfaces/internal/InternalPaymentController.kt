package com.ch.auction.payment.interfaces.internal

import com.ch.auction.common.ApiResponse
import com.ch.auction.payment.application.service.OrderService
import com.ch.auction.payment.interfaces.api.dto.order.OrderResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Internal Payment API", description = "내부 서비스 간 통신 - 결제 API")
@RestController
@RequestMapping("/internal/payments")
class InternalPaymentController(
    private val orderService: OrderService
) {

    @Operation(summary = "경매별 주문 조회", description = "특정 경매의 주문 정보 조회 (service-chat 호출)")
    @GetMapping("/orders")
    fun getOrderByAuctionId(
        @RequestParam auctionId: Long
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        val response = orderService.getOrderByAuctionId(auctionId)
        
        return ResponseEntity.ok(
            ApiResponse.ok(data = response)
        )
    }
    
    @Operation(summary = "정산 내역 조회", description = "정산 내역 조회 (service-admin 호출)")
    @GetMapping("/settlements")
    fun getSettlements(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<ApiResponse<Page<Map<String, Any>>>> {
        // TODO: Settlement 엔티티 및 조회 로직 구현 필요
        val emptyPage = PageImpl<Map<String, Any>>(
            emptyList(),
            PageRequest.of(page, size),
            0
        )
        
        return ResponseEntity.ok(
            ApiResponse.ok(data = emptyPage)
        )
    }
    
    @Operation(summary = "정산 상세 조회", description = "특정 정산 내역 조회 (service-admin 호출)")
    @GetMapping("/settlements/{settlementId}")
    fun getSettlement(
        @PathVariable settlementId: Long
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        // TODO: Settlement 엔티티 및 조회 로직 구현 필요
        val emptyData = mapOf<String, Any>()
        
        return ResponseEntity.ok(
            ApiResponse.ok(data = emptyData)
        )
    }
    
    @Operation(summary = "거래 내역 조회", description = "포인트 거래 내역 조회 (service-admin 호출)")
    @GetMapping("/transactions")
    fun getTransactions(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) userId: Long?
    ): ResponseEntity<ApiResponse<Page<Map<String, Any>>>> {
        // TODO: Transaction 조회 로직 구현 필요
        val emptyPage = PageImpl<Map<String, Any>>(
            emptyList(),
            PageRequest.of(page, size),
            0
        )
        
        return ResponseEntity.ok(
            ApiResponse.ok(data = emptyPage)
        )
    }
}

