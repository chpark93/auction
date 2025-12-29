package com.ch.auction.admin.infrastructure.client.dto

import java.time.LocalDateTime

object PaymentClientDtos {
    data class SettlementResponse(
        val id: Long,
        val auctionId: Long,
        val sellerId: Long,
        val winnerId: Long,
        val finalPrice: Long,
        val commissionAmount: Long,
        val sellerAmount: Long,
        val status: String,
        val createdAt: LocalDateTime
    )
    
    data class SettlementListResponse(
        val content: List<SettlementResponse>,
        val totalElements: Long,
        val totalPages: Int,
        val number: Int,
        val size: Int
    )
    
    data class TransactionResponse(
        val id: Long,
        val userId: Long,
        val type: String,
        val amount: Long,
        val balanceAfter: Long,
        val description: String,
        val createdAt: LocalDateTime
    )
    
    data class TransactionListResponse(
        val content: List<TransactionResponse>,
        val totalElements: Long,
        val totalPages: Int,
        val number: Int,
        val size: Int
    )
}

