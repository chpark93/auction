package com.ch.auction.domain

import com.ch.auction.domain.common.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "payments")
class Payment private constructor(
    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val orderId: String,

    @Column(nullable = false)
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus,

    @OneToMany(mappedBy = "payment", cascade = [CascadeType.ALL], orphanRemoval = true)
    val transactions: MutableList<PaymentTransaction> = mutableListOf(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) : BaseEntity() {

    companion object {
        fun create(
            userId: Long,
            orderId: String,
            amount: BigDecimal,
            status: PaymentStatus = PaymentStatus.PAID
        ): Payment {
            return Payment(
                userId = userId,
                orderId = orderId,
                amount = amount,
                status = status
            )
        }
    }

    fun addTransaction(transaction: PaymentTransaction) {
        this.transactions.add(transaction)
    }
}
