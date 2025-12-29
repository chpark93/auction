package com.ch.auction.auction.infrastructure.client.search

import java.time.LocalDateTime

data class AuctionDocumentDto(
    val id: String,
    val title: String,
    val category: String,
    val sellerName: String,
    val startPrice: Long,
    val currentPrice: Long,
    val bidCount: Int,
    val status: String,
    val thumbnailUrl: String?,
    val createdAt: LocalDateTime,
    val endTime: LocalDateTime
)

