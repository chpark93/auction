package com.ch.auction.domain

import com.ch.auction.domain.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "deliveries")
class Delivery private constructor(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @Column(nullable = false)
    val address: String,

    @Column
    var courierCompany: String? = null,

    @Column
    var trackingNumber: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: DeliveryStatus = DeliveryStatus.PREPARING,

    @Column
    var deliveredAt: LocalDateTime? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) : BaseEntity() {

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
