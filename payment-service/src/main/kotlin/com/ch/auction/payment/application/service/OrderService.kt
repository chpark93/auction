package com.ch.auction.payment.application.service

import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import com.ch.auction.payment.domain.Delivery
import com.ch.auction.payment.domain.Order
import com.ch.auction.payment.infrastructure.persistence.OrderRepository
import com.ch.auction.payment.interfaces.api.dto.order.OrderResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {

    @Transactional
    fun createOrder(
        auctionId: Long,
        buyerId: Long,
        sellerId: Long,
        payment: Long,
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
    
    @Transactional(readOnly = true)
    fun getOrderByAuctionId(
        auctionId: Long
    ): OrderResponse {
        val order = orderRepository.findByAuctionId(
            auctionId = auctionId
        ).orElseThrow { BusinessException(ErrorCode.ORDER_NOT_FOUND) }
        
        return OrderResponse.from(
            order = order
        )
    }
}

