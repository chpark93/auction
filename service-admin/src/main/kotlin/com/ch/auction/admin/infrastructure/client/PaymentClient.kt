package com.ch.auction.admin.infrastructure.client

import com.ch.auction.admin.infrastructure.client.dto.PaymentClientDtos
import com.ch.auction.common.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*

@FeignClient(name = "service-payment")
interface PaymentClient {
    
    @GetMapping("/api/v1/payments/settlements")
    fun getSettlements(
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) status: String?
    ): ApiResponse<PaymentClientDtos.SettlementListResponse>
    
    @GetMapping("/api/v1/payments/settlements/{settlementId}")
    fun getSettlement(
        @PathVariable settlementId: Long
    ): ApiResponse<PaymentClientDtos.SettlementResponse>
    
    @GetMapping("/api/v1/payments/transactions")
    fun getTransactions(
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) userId: Long?
    ): ApiResponse<PaymentClientDtos.TransactionListResponse>
}

