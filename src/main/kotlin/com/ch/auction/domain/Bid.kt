package com.ch.auction.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "bids", indexes = [
    Index(
        name = "idx_bid_auction_id", columnList = "auctionId"
    )
])
class Bid(
    @Column(nullable = false)
    val auctionId: Long,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val amount: Long,

    @Column(nullable = false)
    val bidTime: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
)

