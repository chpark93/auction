package com.ch.auction.admin.interfaces.api

import com.ch.auction.admin.application.service.AdminPaymentService
import com.ch.auction.admin.infrastructure.client.PaymentClient
import com.ch.auction.admin.infrastructure.client.dto.PaymentClientDtos
import com.ch.auction.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Admin Payment API", description = "관리자 결제/정산 관리 API")
@RestController
@RequestMapping("/api/v1/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
class AdminPaymentController(
    private val adminPaymentService: AdminPaymentService,
    private val paymentClient: PaymentClient
) {

    @Operation(summary = "정산 내역 조회", description = "전체 정산 내역을 조회")
    @GetMapping("/settlements")
    fun getSettlements(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<ApiResponse<PaymentClientDtos.SettlementListResponse>> {
        val response = adminPaymentService.getSettlements(
            page = page,
            size = size,
            status = status
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }
    
    @Operation(summary = "정산 상세 조회", description = "특정 정산 내역을 조회")
    @GetMapping("/settlements/{settlementId}")
    fun getSettlement(
        @PathVariable settlementId: Long
    ): ResponseEntity<ApiResponse<PaymentClientDtos.SettlementResponse>> {
        val response = adminPaymentService.getSettlement(
            settlementId = settlementId
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }
    
    @Operation(summary = "포인트 거래 내역 조회", description = "전체 포인트 거래 내역을 조회")
    @GetMapping("/transactions")
    fun getTransactions(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) userId: Long?
    ): ResponseEntity<ApiResponse<PaymentClientDtos.TransactionListResponse>> {
        val response = adminPaymentService.getTransactions(
            page = page,
            size = size,
            userId = userId
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }
}

