package com.ch.auction.payment.domain

import com.ch.auction.common.BaseEntity
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
    val amount: Long,

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
            amount: Long,
            status: PaymentStatus,
            provider: String = "" // TODO: 여러 PG사를 지원할 경우에 맞게 수정
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

