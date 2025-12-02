package com.ch.auction.search.application.dto

object AuctionSearchDtos {
    data class AuctionCreateEvent(
        val id: Long,
        val title: String,
        val category: String,
        val sellerName: String,
        val startPrice: Long,
        val thumbnailUrl: String?,
        val createdAt: String,
        val endTime: String
    )

    data class AuctionUpdateEvent(
        val id: Long,
        val title: String,
        val category: String,
        val endTime: String
    )

    data class BidSuccessEvent(
        val auctionId: Long,
        val currentPrice: Long,
        val bidCount: Int
    )
}