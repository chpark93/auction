package com.ch.auction.domain.repository

import java.math.BigDecimal

interface AuctionRepository {
    /**
     * Redis -> 입찰 시도
     */
    fun tryBid(
        auctionId: Long,
        userId: Long,
        amount: BigDecimal,
        requestTime: Long
    ): BidResult

    /**
     * 경매 정보 DB -> Redis
     */
    fun loadAuctionToRedis(
        auctionId: Long
    )

    fun getAuctionRedisInfo(
        auctionId: Long
    ): AuctionRedisInfo?

    fun deleteAuctionRedisInfo(
        auctionId: Long
    )

    fun expireAuctionRedisInfo(
        auctionId: Long,
        seconds: Long
    )
}

data class AuctionRedisInfo(
    val currentPrice: Long,
    val lastBidderId: Long?
)
