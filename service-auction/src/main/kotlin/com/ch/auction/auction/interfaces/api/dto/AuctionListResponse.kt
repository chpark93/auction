package com.ch.auction.auction.interfaces.api.dto

import com.ch.auction.auction.domain.Auction
import com.ch.auction.auction.domain.AuctionStatus
import java.time.LocalDateTime

data class AuctionListResponse(
    val id: Long,
    val title: String,
    val sellerName: String,
    val startPrice: Long,
    val currentPrice: Long,
    val status: AuctionStatus,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val createdAt: LocalDateTime,
    val uniqueBidders: Int = 0
) {
    companion object {
        fun from(
            auction: Auction,
            sellerName: String,
            uniqueBidders: Int = 0
        ): AuctionListResponse {
            return AuctionListResponse(
                id = auction.id!!,
                title = auction.title,
                sellerName = sellerName,
                startPrice = auction.startPrice,
                currentPrice = auction.currentPrice,
                status = auction.status,
                startTime = auction.startTime,
                endTime = auction.endTime,
                createdAt = auction.createdAt,
                uniqueBidders = uniqueBidders
            )
        }
    }
}

