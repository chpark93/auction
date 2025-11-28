package com.ch.auction.infrastructure.persistence

import com.ch.auction.domain.Auction
import com.ch.auction.domain.AuctionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface AuctionJpaRepository : JpaRepository<Auction, Long> {
    @Modifying
    @Query("UPDATE Auction a SET a.currentPrice = :price WHERE a.id = :id AND a.currentPrice < :price")
    fun updateCurrentPriceIfHigher(
        id: Long,
        price: Long
    ): Int

    fun findAllByStatusAndStartTimeLessThanEqual(
        status: AuctionStatus,
        now: LocalDateTime
    ): List<Auction>

    fun findAllByStatusAndEndTimeLessThanEqual(
        status: AuctionStatus,
        now: LocalDateTime
    ): List<Auction>
}
