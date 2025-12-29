package com.ch.auction.admin.infrastructure.client

import com.ch.auction.admin.infrastructure.client.dto.PaymentClientDtos
import com.ch.auction.common.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "service-payment")
interface PaymentClient {
    
    @GetMapping("/internal/payments/settlements")
    fun getSettlements(
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) status: String?
    ): ApiResponse<PaymentClientDtos.SettlementListResponse>
    
    @GetMapping("/internal/payments/settlements/{settlementId}")
    fun getSettlement(
        @PathVariable settlementId: Long
    ): ApiResponse<PaymentClientDtos.SettlementResponse>
    
    @GetMapping("/internal/payments/transactions")
    fun getTransactions(
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) userId: Long?
    ): ApiResponse<PaymentClientDtos.TransactionListResponse>
}

