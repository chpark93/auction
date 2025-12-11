package com.ch.auction.auction.interfaces.api.dto

import com.ch.auction.auction.domain.Bid
import com.ch.auction.auction.domain.BidStatus
import java.time.LocalDateTime

data class BidHistoryResponse(
    val bidId: Long,
    val userId: Long,
    val userEmail: String,
    val userNickname: String?,
    val amount: Long,
    val bidTime: LocalDateTime,
    val sequence: Long,
    val status: BidStatus
) {
    companion object {
        fun from(
            bid: Bid,
            userEmail: String,
            userNickname: String?
        ): BidHistoryResponse {
            return BidHistoryResponse(
                bidId = bid.id!!,
                userId = bid.userId,
                userEmail = userEmail,
                userNickname = userNickname,
                amount = bid.amount,
                bidTime = bid.bidTime,
                sequence = bid.sequence,
                status = bid.status
            )
        }
    }
}

