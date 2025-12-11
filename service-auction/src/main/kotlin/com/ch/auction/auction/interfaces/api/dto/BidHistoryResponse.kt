package com.ch.auction.auction.interfaces.api.dto

import com.ch.auction.auction.domain.Bid
import java.time.LocalDateTime

data class BidHistoryResponse(
    val bidId: Long,
    val userId: Long,
    val userEmail: String,
    val amount: Long,
    val bidTime: LocalDateTime,
    val sequence: Long
) {
    companion object {
        fun from(
            bid: Bid,
            userEmail: String
        ): BidHistoryResponse {
            return BidHistoryResponse(
                bidId = bid.id!!,
                userId = bid.userId,
                userEmail = userEmail,
                amount = bid.amount,
                bidTime = bid.bidTime,
                sequence = bid.sequence
            )
        }
    }
}

