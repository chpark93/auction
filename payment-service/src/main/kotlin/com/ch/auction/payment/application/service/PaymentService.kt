package com.ch.auction.payment.application.service

import com.ch.auction.common.ErrorCode
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.exception.BusinessException
import com.ch.auction.payment.domain.*
import com.ch.auction.payment.infrastructure.client.UserClient
import com.ch.auction.payment.infrastructure.persistence.PaymentRepository
import com.ch.auction.payment.infrastructure.persistence.PaymentTransactionRepository
import com.ch.auction.payment.infrastructure.persistence.PointTransactionRepository
import com.ch.auction.payment.interfaces.api.dto.payment.PaymentRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentTransactionRepository: PaymentTransactionRepository,
    private val pointTransactionRepository: PointTransactionRepository,
    private val userPointRepository: UserPointRepository,
    private val paymentProvider: PaymentProvider,
    private val userClient: UserClient,
) {

    @Transactional
    fun chargePoint(
        email: String,
        request: PaymentRequest
    ) {
        val userResponse = userClient.getUserByEmail(
            email = email
        ).data ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        if (paymentTransactionRepository.existsByTransactionKey(transactionKey = request.transactionKey)) {
            throw BusinessException(ErrorCode.DUPLICATE_PAYMENT)
        }

        if (!paymentProvider.validatePayment(transactionKey = request.transactionKey, amount = request.amount)) {
            throw BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED)
        }

        val payment = Payment.create(
            userId = userResponse.userId,
            orderId = request.orderId,
            amount = request.amount,
            status = PaymentStatus.PAID
        )
        
        val transaction = PaymentTransaction.create(
            payment = payment,
            transactionKey = request.transactionKey,
            amount = request.amount,
            status = PaymentStatus.PAID
        )

        payment.addTransaction(
            transaction = transaction
        )
        
        paymentRepository.save(payment)

        val updatedUser = userClient.chargePoint(
            userId = userResponse.userId,
            request = PointDTOs.PointRequest(
                amount = request.amount
            )
        ).data ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)
        
        val pointTransaction = PointTransaction.create(
            userId = userResponse.userId,
            type = PointTransactionType.CHARGE,
            amount = request.amount,
            balanceAfter = updatedUser.totalPoint
        )

        pointTransactionRepository.save(pointTransaction)

        userPointRepository.chargePoint(
            userId = userResponse.userId,
            amount = request.amount
        )
    }

    @Transactional
    fun settleAuction(
        userId: Long,
        amount: Long
    ) {
        val updatedUser = userClient.usePoint(
            userId = userId,
            request = PointDTOs.PointRequest(
                amount = amount
            )
        ).data ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
        
        val transaction = PointTransaction.create(
            userId = userId,
            type = PointTransactionType.USE,
            amount = amount,
            balanceAfter = updatedUser.totalPoint
        )

        pointTransactionRepository.save(transaction)

        userPointRepository.usePoint(
            userId = userId,
            amount = amount
        )
    }
}
