package com.ch.auction.auction.infrastructure.persistence

import com.ch.auction.auction.domain.Bid
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface BidJpaRepository : JpaRepository<Bid, Long> {
    fun findByAuctionIdOrderByBidTimeDesc(
        auctionId: Long,
        pageable: Pageable
    ): List<Bid>
}

