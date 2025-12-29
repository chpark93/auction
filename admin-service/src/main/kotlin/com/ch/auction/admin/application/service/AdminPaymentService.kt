package com.ch.auction.admin.application.service

import com.ch.auction.admin.infrastructure.client.PaymentClient
import com.ch.auction.admin.infrastructure.client.dto.PaymentClientDtos
import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import org.springframework.stereotype.Service

@Service
class AdminPaymentService(
    private val paymentClient: PaymentClient
) {
    /**
     * 정산 내역 조회
     */
    fun getSettlements(
        page: Int = 0,
        size: Int = 20,
        status: String?
    ): PaymentClientDtos.SettlementListResponse {
        val response = paymentClient.getSettlements(
            page = page,
            size = size,
            status = status
        )
        
        return response.data ?: PaymentClientDtos.SettlementListResponse(
            content = emptyList(),
            totalElements = 0,
            totalPages = 0,
            number = page,
            size = size
        )
    }
    
    /**
     * 정산 상세 조회
     */
    fun getSettlement(
        settlementId: Long
    ): PaymentClientDtos.SettlementResponse {
        val response = paymentClient.getSettlement(
            settlementId = settlementId
        )
        
        return response.data ?: throw BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND)
    }
    
    /**
     * 포인트 거래 내역 조회
     */
    fun getTransactions(
        page: Int = 0,
        size: Int = 20,
        userId: Long?
    ): PaymentClientDtos.TransactionListResponse {
        val response = paymentClient.getTransactions(
            page = page,
            size = size,
            userId = userId
        )
        
        return response.data ?: PaymentClientDtos.TransactionListResponse(
            content = emptyList(),
            totalElements = 0,
            totalPages = 0,
            number = page,
            size = size
        )
    }
}
