package com.ch.auction.application.service

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.domain.Payment
import com.ch.auction.domain.PaymentStatus
import com.ch.auction.domain.PaymentTransaction
import com.ch.auction.domain.PointTransaction
import com.ch.auction.domain.model.PointTransactionType
import com.ch.auction.domain.payment.PaymentProvider
import com.ch.auction.domain.repository.UserPointRepository
import com.ch.auction.infrastructure.persistence.PaymentRepository
import com.ch.auction.infrastructure.persistence.PaymentTransactionRepository
import com.ch.auction.infrastructure.persistence.PointTransactionRepository
import com.ch.auction.infrastructure.persistence.UserRepository
import com.ch.auction.interfaces.api.dto.payment.PaymentRequest
import com.ch.auction.interfaces.common.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentTransactionRepository: PaymentTransactionRepository,
    private val pointTransactionRepository: PointTransactionRepository,
    private val userRepository: UserRepository,
    private val userPointRepository: UserPointRepository,
    private val paymentProvider: PaymentProvider
) {

    @Transactional
    fun chargePoint(
        email: String,
        request: PaymentRequest
    ) {
        val user = userRepository.findByEmail(
            email = email
        ).orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        if (paymentTransactionRepository.existsByTransactionKey(transactionKey = request.transactionKey)) {
            throw BusinessException(ErrorCode.DUPLICATE_PAYMENT)
        }

        if (!paymentProvider.validatePayment(transactionKey = request.transactionKey, amount = request.amount)) {
            throw BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED)
        }

        val payment = Payment.create(
            userId = user.id!!,
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

        user.chargePoint(
            amount = request.amount
        )
        
        val pointTransaction = PointTransaction.create(
            userId = user.id,
            type = PointTransactionType.CHARGE,
            amount = request.amount,
            balanceAfter = user.point
        )

        pointTransactionRepository.save(pointTransaction)

        userPointRepository.chargePoint(user.id, request.amount.toLong())
    }

    @Transactional
    fun settleAuction(
        userId: Long,
        amount: BigDecimal
    ) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        user.usePoint(
            amount = amount
        )
        
        val transaction = PointTransaction.create(
            userId = userId,
            type = PointTransactionType.USE,
            amount = amount,
            balanceAfter = user.point
        )

        pointTransactionRepository.save(transaction)

        userPointRepository.usePoint(
            userId = userId,
            amount = amount.toLong()
        )
    }
}
