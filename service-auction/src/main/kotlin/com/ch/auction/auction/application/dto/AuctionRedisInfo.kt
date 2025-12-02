package com.ch.auction.auction.application.dto


object AuctionRedisDtos {

    data class AuctionRedisInfo(
        val currentPrice: Long,
        val lastBidderId: Long?
    )
}