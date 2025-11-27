package com.ch.auction.domain.event

import java.math.BigDecimal
import java.time.LocalDateTime

data class BidSuccessEvent(
    val auctionId: Long,
    val userId: Long,
    val amount: BigDecimal,
    val bidTime: LocalDateTime
)

