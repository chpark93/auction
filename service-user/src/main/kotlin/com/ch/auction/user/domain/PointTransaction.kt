package com.ch.auction.user.domain

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@Table(
    name = "point_transactions",
    indexes = [
        Index(name = "idx_point_tx_user_id", columnList = "userId"),
        Index(name = "idx_point_tx_auction_id", columnList = "auctionId"),
        Index(name = "idx_point_tx_status", columnList = "status")
    ]
)
@SQLRestriction("deleted = false")
class PointTransaction private constructor(
    @Column(nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val type: TransactionType,

    @Column(nullable = false)
    val amount: Long,

    @Column(nullable = false)
    val balanceAfter: Long,

    @Column(nullable = false, length = 500)
    val reason: String,

    @Column
    val auctionId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: TransactionStatus = TransactionStatus.COMPLETED,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
    @Column(nullable = false)
    var deleted: Boolean = false
        private set

    companion object {
        fun create(
            userId: Long,
            type: TransactionType,
            amount: Long,
            balanceAfter: Long,
            reason: String,
            auctionId: Long? = null,
            status: TransactionStatus = TransactionStatus.COMPLETED
        ): PointTransaction {
            return PointTransaction(
                userId = userId,
                type = type,
                amount = amount,
                balanceAfter = balanceAfter,
                reason = reason,
                auctionId = auctionId,
                status = status
            )
        }
    }

    fun complete() {
        this.status = TransactionStatus.COMPLETED
    }

    fun cancel() {
        this.status = TransactionStatus.CANCELLED
    }

    fun delete() {
        this.deleted = true
    }
}

