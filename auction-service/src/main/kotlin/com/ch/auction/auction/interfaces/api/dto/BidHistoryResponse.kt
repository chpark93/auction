package com.ch.auction.auction.interfaces.api.dto

import com.ch.auction.auction.domain.Bid
import com.ch.auction.auction.domain.BidStatus
import java.time.LocalDateTime

data class BidHistoryResponse(
    val bidId: Long,
    val amount: Long,
    val bidTime: LocalDateTime,
    val sequence: Long,
    val status: BidStatus
) {
    companion object {
        fun from(
            bid: Bid
        ): BidHistoryResponse {
            return BidHistoryResponse(
                bidId = bid.id!!,
                amount = bid.amount,
                bidTime = bid.bidTime,
                sequence = bid.sequence,
                status = bid.status
            )
        }
    }
}

