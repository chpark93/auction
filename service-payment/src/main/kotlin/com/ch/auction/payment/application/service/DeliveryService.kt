package com.ch.auction.payment.application.service

import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import com.ch.auction.payment.domain.DeliveryStatus
import com.ch.auction.payment.infrastructure.persistence.OrderRepository
import com.ch.auction.payment.interfaces.api.dto.delivery.ShippingRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeliveryService(
    private val orderRepository: OrderRepository
) {

    @Transactional
    fun startShipping(
        sellerId: Long,
        orderId: Long,
        request: ShippingRequest
    ) {
        val order = orderRepository.findById(orderId)
            .orElseThrow { BusinessException(ErrorCode.ORDER_NOT_FOUND) }

        if (order.sellerId != sellerId) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        val delivery = order.delivery ?: throw BusinessException(ErrorCode.DELIVERY_NOT_FOUND)

        if (delivery.status != DeliveryStatus.PREPARING) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        delivery.startShipping(
            courierCompany = request.courierCompany,
            trackingNumber = request.trackingNumber
        )
    }
}

