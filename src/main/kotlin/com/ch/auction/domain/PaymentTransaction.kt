package com.ch.auction.domain

import com.ch.auction.domain.common.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "payment_transactions")
class PaymentTransaction private constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    val payment: Payment,

    @Column(nullable = false, unique = true)
    val transactionKey: String,

    @Column(nullable = false)
    val amount: BigDecimal,

    @Column(nullable = false)
    val provider: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: PaymentStatus,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) : BaseEntity() {
    companion object {
        fun create(
            payment: Payment,
            transactionKey: String,
            amount: BigDecimal,
            status: PaymentStatus,
            provider: String = "PORTONE"
        ): PaymentTransaction {
            return PaymentTransaction(
                payment = payment,
                transactionKey = transactionKey,
                amount = amount,
                status = status,
                provider = provider
            )
        }
    }
}

