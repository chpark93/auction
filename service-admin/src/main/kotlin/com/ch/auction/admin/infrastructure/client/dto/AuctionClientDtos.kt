package com.ch.auction.admin.infrastructure.client.dto

import java.time.LocalDateTime

object AuctionClientDtos {
    data class AuctionResponse(
        val id: Long,
        val title: String,
        val sellerId: Long,
        val sellerName: String?,
        val startPrice: Long,
        val currentPrice: Long,
        val status: String,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val createdAt: LocalDateTime
    )
    
    data class AuctionListResponse(
        val content: List<AuctionResponse>,
        val totalElements: Long,
        val totalPages: Int,
        val number: Int,
        val size: Int
    )
    
    data class RejectRequest(
        val reason: String
    )
}

