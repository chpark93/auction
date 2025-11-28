package com.ch.auction.domain

import com.ch.auction.domain.common.BaseEntity
import com.ch.auction.domain.model.PointTransactionType
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "point_transactions")
class PointTransaction private constructor(
    @Column(nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PointTransactionType,

    @Column(nullable = false)
    val amount: BigDecimal,

    @Column(nullable = false)
    val balanceAfter: BigDecimal,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) : BaseEntity() {
    companion object {
        fun create(
            userId: Long,
            type: PointTransactionType,
            amount: BigDecimal,
            balanceAfter: BigDecimal
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

