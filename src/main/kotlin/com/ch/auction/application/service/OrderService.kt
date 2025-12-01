package com.ch.auction.application.service

import com.ch.auction.domain.Delivery
import com.ch.auction.domain.Order
import com.ch.auction.infrastructure.persistence.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {

    @Transactional
    fun createOrder(
        auctionId: Long,
        buyerId: Long,
        sellerId: Long,
        payment: BigDecimal,
        // TODO: User의 기본 배송지를 조회 -> 전달
        address: String = "Default Address"
    ): Order {
        val order = Order.create(
            auctionId = auctionId,
            buyerId = buyerId,
            sellerId = sellerId,
            payment = payment
        )

        val delivery = Delivery.create(
            order = order,
            address = address
        )

        order.assignDelivery(
            delivery = delivery
        )
        
        return orderRepository.save(order)
    }
}

