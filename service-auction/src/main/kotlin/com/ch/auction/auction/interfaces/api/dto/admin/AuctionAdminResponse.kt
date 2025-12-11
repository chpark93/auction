package com.ch.auction.auction.interfaces.api.dto.admin

import com.ch.auction.auction.domain.Auction
import java.time.LocalDateTime

data class AuctionAdminResponse(
    val id: Long,
    val title: String,
    val sellerId: Long,
    val sellerName: String?,
    val startPrice: Long,
    val currentPrice: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val status: String,
    val createdAt: LocalDateTime
) {

    companion object {
        fun from(
            auction: Auction
        ): AuctionAdminResponse {
            return AuctionAdminResponse(
                id = auction.id!!,
                title = auction.title,
                sellerId = auction.sellerId,
                sellerName = null, // TODO: Feign으로 조회 또는 이벤트 동기화
                startPrice = auction.startPrice,
                currentPrice = auction.currentPrice,
                startTime = auction.startTime,
                endTime = auction.endTime,
                status = auction.status.name,
                createdAt = auction.createdAt
            )
        }
    }
}

