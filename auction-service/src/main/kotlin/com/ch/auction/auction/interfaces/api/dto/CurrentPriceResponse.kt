package com.ch.auction.auction.interfaces.api.dto

data class CurrentPriceResponse(
    val currentPrice: Long,
    val uniqueBidders: Int,
    val bidCount: Int
)

