package com.ch.auction.payment.domain

import com.ch.auction.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "deliveries")
class Delivery private constructor(
    courierCompany: String? = null,
    trackingNumber: String? = null,
    status: DeliveryStatus = DeliveryStatus.PREPARING,
    deliveredAt: LocalDateTime? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @Column(nullable = false)
    val address: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) : BaseEntity() {

    @Column
    var courierCompany: String? = courierCompany
        private set

    @Column
    var trackingNumber: String? = trackingNumber
        private set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: DeliveryStatus = status
        private set

    @Column
    var deliveredAt: LocalDateTime? = deliveredAt
        private set

    companion object {
        fun create(
            order: Order,
            address: String
        ): Delivery {
            return Delivery(
                order = order,
                address = address
            )
        }
    }

    fun startShipping(
        courierCompany: String,
        trackingNumber: String
    ) {
        this.courierCompany = courierCompany
        this.trackingNumber = trackingNumber
        this.status = DeliveryStatus.SHIPPING
    }

    fun completeDelivery() {
        this.status = DeliveryStatus.DELIVERED
        this.deliveredAt = LocalDateTime.now()
    }

    fun confirm() {
        this.status = DeliveryStatus.CONFIRMED
    }
}
