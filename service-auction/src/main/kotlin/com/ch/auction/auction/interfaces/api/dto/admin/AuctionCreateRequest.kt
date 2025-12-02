package com.ch.auction.auction.interfaces.api.dto.admin

import java.time.LocalDateTime

data class AuctionCreateRequest(
    val title: String,
    val startPrice: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)

