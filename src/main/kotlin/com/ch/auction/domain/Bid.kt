package com.ch.auction.domain

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@Table(name = "bids", indexes = [
    Index(
        name = "idx_bid_auction_id", columnList = "auctionId"
    )
])
@SQLRestriction("deleted = false")
class Bid private constructor(
    @Column(nullable = false)
    val auctionId: Long,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val amount: Long,

    @Column(nullable = false)
    val bidTime: LocalDateTime,

    @Column(nullable = false)
    val sequence: Long,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
    @Column(nullable = false)
    var deleted: Boolean = false
        private set

    companion object {
        fun create(
            auctionId: Long,
            userId: Long,
            amount: Long,
            bidTime: LocalDateTime = LocalDateTime.now(),
            sequence: Long
        ): Bid {
            return Bid(
                auctionId = auctionId,
                userId = userId,
                amount = amount,
                bidTime = bidTime,
                sequence = sequence
            )
        }
    }

    fun delete() {
        this.deleted = true
    }
}
