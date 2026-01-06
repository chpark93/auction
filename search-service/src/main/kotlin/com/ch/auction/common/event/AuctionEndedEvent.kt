package com.ch.auction.common.event

import java.time.LocalDateTime

data class AuctionEndedEvent(
    val auctionId: Long,
    val sellerId: Long,
    val winnerId: Long?,
    val finalPrice: Long,
    val endedAt: LocalDateTime
)
