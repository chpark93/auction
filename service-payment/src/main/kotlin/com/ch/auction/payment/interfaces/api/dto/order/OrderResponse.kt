package com.ch.auction.payment.interfaces.api.dto.order

import com.ch.auction.payment.domain.Order
import java.time.LocalDateTime

data class OrderResponse(
    val orderId: Long,
    val auctionId: Long,
    val buyerId: Long,
    val sellerId: Long,
    val payment: Long,
    val deliveryStatus: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(
            order: Order
        ): OrderResponse {
            return OrderResponse(
                orderId = order.id!!,
                auctionId = order.auctionId,
                buyerId = order.buyerId,
                sellerId = order.sellerId,
                payment = order.payment,
                deliveryStatus = order.delivery?.status?.name,
                createdAt = order.createdAt
            )
        }
    }
}

