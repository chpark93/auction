package com.ch.auction.interfaces.api.dto

import java.math.BigDecimal

data class BidRequest(
    val userId: Long,
    val amount: BigDecimal
)
