package com.ch.auction.user.application.service

import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import com.ch.auction.user.domain.PointTransaction
import com.ch.auction.user.domain.TransactionStatus
import com.ch.auction.user.domain.TransactionType
import com.ch.auction.user.infrastructure.persistence.PointTransactionRepository
import com.ch.auction.user.infrastructure.persistence.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PointTransactionService(
    private val pointTransactionRepository: PointTransactionRepository,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 경매 낙찰 시 처리
     * - 낙찰자: HOLD -> USE (결제 확정)
     * - 다른 입찰자: HOLD -> RELEASE (환불)
     */
    @Transactional
    fun processAuctionCompleted(
        auctionId: Long,
        winnerId: Long,
        finalPrice: Long
    ) {
        val pendingTransactions = pointTransactionRepository.findByAuctionIdAndStatus(
            auctionId = auctionId,
            status = TransactionStatus.PENDING
        )

        pendingTransactions.forEach { holdTransaction ->
            if (holdTransaction.userId == winnerId) {
                // 낙찰자: HOLD -> USE (실제 포인트 차감 + lock 해제)
                holdTransaction.complete()
                
                val user = userRepository.findById(winnerId)
                    .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
                
                user.confirmPoint(holdTransaction.amount)
                
                pointTransactionRepository.save(
                    PointTransaction.create(
                        userId = winnerId,
                        type = TransactionType.USE,
                        amount = holdTransaction.amount,
                        balanceAfter = user.point,
                        reason = "경매 낙찰 (경매 ID: $auctionId)",
                        auctionId = auctionId,
                        status = TransactionStatus.COMPLETED
                    )
                )
                
                logger.info("Point confirmed for winner $winnerId: ${holdTransaction.amount}")
            } else {
                // 다른 입찰자: HOLD -> RELEASE (lock 해제만)
                releaseHoldPoint(
                    auctionId = auctionId,
                    holdTransaction = holdTransaction
                )
            }
        }
    }

    /**
     * 경매 유찰 시 처리
     * - 모든 입찰자: HOLD -> RELEASE (환불)
     */
    @Transactional
    fun processAuctionFailed(
        auctionId: Long
    ) {
        val pendingTransactions = pointTransactionRepository.findByAuctionIdAndStatus(
            auctionId = auctionId,
            status = TransactionStatus.PENDING
        )

        pendingTransactions.forEach { holdTransaction ->
            releaseHoldPoint(
                auctionId = auctionId,
                holdTransaction = holdTransaction
            )
        }
    }
    
    /**
     * 사용자 포인트 내역 조회
     */
    @Transactional(readOnly = true)
    fun getUserPointHistory(
        userId: Long,
        page: Int = 0,
        size: Int = 20
    ): Page<PointTransaction> {
        return pointTransactionRepository.findByUserIdOrderByCreatedAtDesc(
            userId = userId,
            pageable = PageRequest.of(page, size)
        )
    }

    private fun releaseHoldPoint(
        auctionId: Long,
        holdTransaction: PointTransaction
    ) {
        holdTransaction.complete()

        val user = userRepository.findById(holdTransaction.userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        // 락만 해제 (point는 그대로 유지)
        user.releasePoint(holdTransaction.amount)

        pointTransactionRepository.save(
            PointTransaction.create(
                userId = holdTransaction.userId,
                type = TransactionType.RELEASE,
                amount = holdTransaction.amount,
                balanceAfter = user.point,
                reason = "경매 유찰 환불 (경매 ID: ${holdTransaction.auctionId})",
                auctionId = auctionId,
                status = TransactionStatus.COMPLETED
            )
        )

        logger.info("Point released for user ${holdTransaction.userId}: ${holdTransaction.amount}")
    }
    
    private fun getUserBalance(
        userId: Long
    ): Long {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        return user.point
    }
}

