package com.ch.auction.auction.interfaces.api.dto

import com.ch.auction.auction.domain.Auction
import com.ch.auction.auction.domain.AuctionStatus
import java.time.LocalDateTime

data class AuctionResponse(
    val id: Long,
    val title: String,
    val startPrice: Long,
    val currentPrice: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val sellerId: Long,
    val sellerName: String,
    val status: AuctionStatus,
    val uniqueBidders: Int = 0,
    val bidCount: Int = 0
) {
    companion object {
        fun from(
            auction: Auction,
            sellerName: String,
            uniqueBidders: Int = 0,
            bidCount: Int = 0
        ): AuctionResponse {
            return AuctionResponse(
                id = auction.id!!,
                title = auction.title,
                startPrice = auction.startPrice,
                currentPrice = auction.currentPrice,
                startTime = auction.startTime,
                endTime = auction.endTime,
                sellerId = auction.sellerId,
                sellerName = sellerName,
                status = auction.status,
                uniqueBidders = uniqueBidders,
                bidCount = bidCount
            )
        }
    }
}

