package com.ch.auction.user.interfaces.api.dto

import com.ch.auction.user.domain.PointTransaction
import com.ch.auction.user.domain.TransactionStatus
import com.ch.auction.user.domain.TransactionType
import java.time.LocalDateTime

data class PointTransactionResponse(
    val id: Long,
    val type: TransactionType,
    val amount: Long,
    val balanceAfter: Long,
    val reason: String,
    val auctionId: Long?,
    val status: TransactionStatus,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(
            transaction: PointTransaction
        ): PointTransactionResponse {
            return PointTransactionResponse(
                id = transaction.id!!,
                type = transaction.type,
                amount = transaction.amount,
                balanceAfter = transaction.balanceAfter,
                reason = transaction.reason,
                auctionId = transaction.auctionId,
                status = transaction.status,
                createdAt = transaction.createdAt
            )
        }
    }
}

