package com.ch.auction.application.service

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.domain.DeliveryStatus
import com.ch.auction.domain.PointTransaction
import com.ch.auction.domain.model.PointTransactionType
import com.ch.auction.domain.repository.UserPointRepository
import com.ch.auction.infrastructure.persistence.OrderRepository
import com.ch.auction.infrastructure.persistence.PointTransactionRepository
import com.ch.auction.infrastructure.persistence.UserRepository
import com.ch.auction.interfaces.common.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class SettlementService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val pointTransactionRepository: PointTransactionRepository,
    private val userPointRepository: UserPointRepository
) {

    @Transactional
    fun confirmPurchase(
        buyerId: Long,
        orderId: Long
    ) {
        val order = orderRepository.findById(orderId)
            .orElseThrow { BusinessException(ErrorCode.ORDER_NOT_FOUND) }

        if (order.buyerId != buyerId) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        val delivery = order.delivery ?: throw BusinessException(ErrorCode.DELIVERY_NOT_FOUND)

        // 이미 확정된 경우
        if (delivery.status == DeliveryStatus.CONFIRMED) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        delivery.confirm()

        processSettlement(
            sellerId = order.sellerId,
            amount = order.payment
        )
    }

    private fun processSettlement(
        sellerId: Long,
        amount: BigDecimal
    ) {
        // TODO: 정산 수수료 정책 변경 -> 수정 필요
        val commissionRate = BigDecimal("0.05")
        val commission = amount.multiply(commissionRate)
        val settlementAmount = amount.subtract(commission)

        val seller = userRepository.findById(sellerId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        // 판매자 포인트 지급
        seller.chargePoint(
            amount = settlementAmount
        )
        
        // Point transaction 저장
        val transaction = PointTransaction.create(
            userId = sellerId,
            type = PointTransactionType.SETTLEMENT,
            amount = settlementAmount,
            balanceAfter = seller.point
        )

        pointTransactionRepository.save(transaction)

        // Redis 동기화
        userPointRepository.chargePoint(
            userId = sellerId,
            amount = settlementAmount.toLong()
        )
    }
}

