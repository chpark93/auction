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
class Bid(
    @Column(nullable = false)
    val auctionId: Long,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val amount: Long,

    @Column(nullable = false)
    val bidTime: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val sequence: Long, // Redis에서 발급한 순서 번호

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
    @Column(nullable = false)
    var deleted: Boolean = false
        private set

    fun delete() {
        this.deleted = true
    }
}
