package com.ch.auction.auction.domain

import com.ch.auction.auction.application.dto.AuctionRedisDtos

interface AuctionRepository {
    /**
     * 입찰 시도
     */
    fun tryBid(
        auctionId: Long,
        userId: Long,
        amount: Long,
        requestTime: Long,
        userPoint: Long
    ): BidResult

    /**
     * 경매 정보 DB -> Redis
     */
    fun loadAuctionToRedis(
        auctionId: Long
    )

    fun getAuctionRedisInfo(
        auctionId: Long
    ): AuctionRedisDtos.AuctionRedisInfo?

    fun deleteAuctionRedisInfo(
        auctionId: Long
    )

    fun expireAuctionRedisInfo(
        auctionId: Long,
        seconds: Long
    )
}
