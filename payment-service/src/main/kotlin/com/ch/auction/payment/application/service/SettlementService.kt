package com.ch.auction.payment.application.service

import com.ch.auction.common.ErrorCode
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.exception.BusinessException
import com.ch.auction.payment.domain.DeliveryStatus
import com.ch.auction.payment.domain.PointTransaction
import com.ch.auction.payment.domain.PointTransactionType
import com.ch.auction.payment.domain.UserPointRepository
import com.ch.auction.payment.infrastructure.client.UserClient
import com.ch.auction.payment.infrastructure.persistence.OrderRepository
import com.ch.auction.payment.infrastructure.persistence.PointTransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class SettlementService(
    private val orderRepository: OrderRepository,
    private val pointTransactionRepository: PointTransactionRepository,
    private val userPointRepository: UserPointRepository,
    private val userClient: UserClient
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
        amount: Long
    ) {
        // TODO: 정산 수수료 정책 변경 -> 수정 필요
        // 5% commission logic
        val commissionRate = 0.05
        val commission = (amount * commissionRate).toLong()
        val settlementAmount = amount - commission

        // 판매자 포인트 지급
        val updatedUser = userClient.chargePoint(
            userId = sellerId,
            request = PointDTOs.PointRequest(
                amount = settlementAmount
            )
        ).data ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)
        
        // Point transaction 저장
        val transaction = PointTransaction.create(
            userId = sellerId,
            type = PointTransactionType.SETTLEMENT,
            amount = settlementAmount,
            balanceAfter = updatedUser.totalPoint
        )

        pointTransactionRepository.save(transaction)

        // Redis 동기화
        userPointRepository.chargePoint(
            userId = sellerId,
            amount = settlementAmount
        )
    }
}
