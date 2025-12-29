package com.ch.auction.payment.domain

import com.ch.auction.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "point_transactions")
class PointTransaction private constructor(
    @Column(nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PointTransactionType,

    @Column(nullable = false)
    val amount: Long,

    @Column(nullable = false)
    val balanceAfter: Long,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) : BaseEntity() {
    companion object {
        fun create(
            userId: Long,
            type: PointTransactionType,
            amount: Long,
            balanceAfter: Long
        ): PointTransaction {
            return PointTransaction(
                userId = userId,
                type = type,
                amount = amount,
                balanceAfter = balanceAfter
            )
        }
    }
}

