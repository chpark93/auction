package com.ch.auction.domain

import com.ch.auction.domain.common.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "orders")
class Order private constructor(
    @Column(nullable = false)
    val auctionId: Long,

    @Column(nullable = false)
    val buyerId: Long,

    @Column(nullable = false)
    val sellerId: Long,

    @Column(nullable = false)
    val payment: BigDecimal,

    @OneToOne(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var delivery: Delivery? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) : BaseEntity() {
    
    companion object {
        fun create(
            auctionId: Long,
            buyerId: Long,
            sellerId: Long,
            payment: BigDecimal
        ): Order {
            return Order(
                auctionId = auctionId,
                buyerId = buyerId,
                sellerId = sellerId,
                payment = payment
            )
        }
    }

    fun assignDelivery(
        delivery: Delivery
    ) {
        this.delivery = delivery
    }
}

