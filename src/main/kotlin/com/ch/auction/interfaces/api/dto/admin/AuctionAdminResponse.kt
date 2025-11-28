package com.ch.auction.interfaces.api.dto.admin

import com.ch.auction.domain.Auction
import com.ch.auction.domain.AuctionStatus
import java.time.LocalDateTime

data class AuctionAdminResponse(
    val id: Long,
    val title: String,
    val startPrice: Long,
    val currentPrice: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val status: AuctionStatus
) {

    companion object {
        fun from(
            auction: Auction
        ): AuctionAdminResponse {
            return AuctionAdminResponse(
                id = auction.id!!,
                title = auction.title,
                startPrice = auction.startPrice,
                currentPrice = auction.currentPrice,
                startTime = auction.startTime,
                endTime = auction.endTime,
                status = auction.status
            )
        }
    }
}

