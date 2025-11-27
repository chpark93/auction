package com.ch.auction.infrastructure.persistence

import com.ch.auction.domain.Auction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface AuctionJpaRepository : JpaRepository<Auction, Long> {
    @Modifying
    @Query("UPDATE Auction a SET a.currentPrice = :price WHERE a.id = :id AND a.currentPrice < :price")
    fun updateCurrentPriceIfHigher(
        id: Long,
        price: Long
    ): Int
}

