package com.ch.auction.payment.domain

import com.ch.auction.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "orders")
class Order private constructor(
    delivery: Delivery? = null,

    @Column(nullable = false)
    val auctionId: Long,

    @Column(nullable = false)
    val buyerId: Long,

    @Column(nullable = false)
    val sellerId: Long,

    @Column(nullable = false)
    val payment: Long,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) : BaseEntity() {

    @OneToOne(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var delivery: Delivery? = delivery
        private set
    
    companion object {
        fun create(
            auctionId: Long,
            buyerId: Long,
            sellerId: Long,
            payment: Long
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
