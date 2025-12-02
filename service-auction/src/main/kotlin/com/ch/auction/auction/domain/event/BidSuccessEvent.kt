package com.ch.auction.auction.domain.event

import java.time.LocalDateTime

data class BidSuccessEvent(
    val auctionId: Long,
    val userId: Long,
    val amount: Long,
    val bidTime: LocalDateTime,
    val sequence: Long
)
