package com.ch.auction.auction.infrastructure.persistence

import com.ch.auction.auction.domain.Bid
import com.ch.auction.auction.domain.BidStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface BidJpaRepository : JpaRepository<Bid, Long> {
    fun findByAuctionIdOrderByBidTimeDesc(
        auctionId: Long,
        pageable: Pageable
    ): List<Bid>
    
    fun findByAuctionIdAndUserIdAndStatus(
        auctionId: Long,
        userId: Long,
        status: BidStatus
    ): List<Bid>
    
    fun findByAuctionIdAndStatusOrderByAmountDesc(
        auctionId: Long,
        status: BidStatus
    ): List<Bid>
    
    fun findByUserIdAndStatusOrderByBidTimeDesc(
        userId: Long,
        status: BidStatus,
        pageable: Pageable
    ): List<Bid>
}

