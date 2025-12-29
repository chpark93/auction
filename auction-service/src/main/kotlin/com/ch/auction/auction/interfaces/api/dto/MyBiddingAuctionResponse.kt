package com.ch.auction.auction.interfaces.api.dto

import com.ch.auction.auction.domain.AuctionStatus
import com.ch.auction.auction.domain.BidStatus
import java.time.LocalDateTime

data class MyBiddingAuctionResponse(
    val auctionId: Long,
    val auctionTitle: String,
    val auctionStatus: AuctionStatus,
    val currentPrice: Long,
    val myBidAmount: Long,
    val myBidStatus: BidStatus,
    val myBidTime: LocalDateTime,
    val isHighestBidder: Boolean,
    val lockedPoint: Long,
    val endTime: LocalDateTime,
    val canCancel: Boolean
)

