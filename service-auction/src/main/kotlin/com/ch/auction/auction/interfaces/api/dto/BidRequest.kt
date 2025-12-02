package com.ch.auction.auction.interfaces.api.dto

data class BidRequest(
    val userId: Long,
    val amount: Long
)
