package com.ch.auction.auction.application.service

import com.ch.auction.auction.application.service.dto.BidDtos
import com.ch.auction.auction.domain.*
import com.ch.auction.auction.infrastructure.client.user.UserClient
import com.ch.auction.auction.infrastructure.client.user.dtos.UserClientDtos
import com.ch.auction.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.auction.infrastructure.persistence.BidJpaRepository
import com.ch.auction.auction.infrastructure.sse.SseEmitterManager
import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
class BidCancellationService(
    private val auctionRepository: AuctionRepository,
    private val auctionJpaRepository: AuctionJpaRepository,
    private val bidJpaRepository: BidJpaRepository,
    private val userClient: UserClient,
    private val sseEmitterManager: SseEmitterManager
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val FREE_CANCEL_MINUTES = 5L  // 무료 취소 가능 시간
        const val NO_CANCEL_BEFORE_END_MINUTES = 10L  // 경매 종료 전 취소 불가 시간
        const val CANCEL_FEE_RATE = 0.01  // 취소 수수료 1%
    }

    /**
     * 입찰 포기
     */
    @Transactional
    fun cancelBid(
        auctionId: Long,
        userId: Long,
        reason: String?
    ): BidDtos.CancelBidResult {
        val auction = auctionJpaRepository.findById(auctionId)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }

        if (auction.status != AuctionStatus.ONGOING) {
            throw BusinessException(ErrorCode.AUCTION_NOT_ONGOING)
        }

        val userActiveBids = bidJpaRepository.findByAuctionIdAndUserIdAndStatus(
            auctionId = auctionId,
            userId = userId,
            status = BidStatus.ACTIVE
        )

        if (userActiveBids.isEmpty()) {
            throw BusinessException(ErrorCode.BID_NOT_FOUND)
        }

        val latestBid = userActiveBids.maxByOrNull { it.bidTime }!!

        // 현재 경매 상태 확인
        val redisInfo = auctionRepository.getAuctionRedisInfo(
            auctionId = auctionId
        ) ?: throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)

        val isHighestBidder = redisInfo.lastBidderId == userId

        // 입찰 포기 가능 여부 검증
        val cancelPolicy = validateCancellation(
            auction = auction,
            bid = latestBid,
            isHighestBidder = isHighestBidder
        )

        // 해당 경매의 모든 입찰 취소
        userActiveBids.forEach { it.cancel() }

        // 포인트 환불
        val totalRefundAmount = latestBid.amount
        val actualRefundAmount = totalRefundAmount - cancelPolicy.fee

        try {
            // lock amount 해제
            userClient.releasePoint(
                userId = userId,
                request = UserClientDtos.ReleasePointRequest(
                    amount = totalRefundAmount,
                    reason = "입찰 포기 (경매 ID: $auctionId)",
                    auctionId = auctionId
                )
            )

            // 수수료가 있는 경우 포인트에서 차감
            // TODO: Point Transaction 처리 필요
            if (cancelPolicy.fee > 0) {
                userClient.deductPoint(
                    userId = userId,
                    amount = cancelPolicy.fee,
                    reason = "입찰 포기 수수료 (경매 ID: $auctionId)"
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to process point refund for bid cancellation", e)
            throw BusinessException(ErrorCode.USER_SERVICE_UNAVAILABLE)
        }

        // Redis 업데이트 (최고 입찰자의 경우 롤백)
        val newCurrentPrice = if (isHighestBidder) {
            rollbackToNextHighestBid(
                auctionId = auctionId,
                cancelledUserId = userId
            )
        } else {
            redisInfo.currentPrice
        }

        sseEmitterManager.sendToAuction(
            auctionId = auctionId,
            eventName = "bid-cancelled",
            data = mapOf(
                "auctionId" to auctionId,
                "userId" to userId,
                "amount" to latestBid.amount,
                "currentPrice" to newCurrentPrice
            )
        )

        logger.info("All bids cancelled for auction $auctionId by user $userId: ${userActiveBids.size} bids, amount=${latestBid.amount}, fee=${cancelPolicy.fee}")

        return BidDtos.CancelBidResult(
            success = true,
            refundAmount = actualRefundAmount,
            fee = cancelPolicy.fee,
            reason = cancelPolicy.message
        )
    }

    /**
     * 포기 가능 여부 검증
     */
    private fun validateCancellation(
        auction: Auction,
        bid: Bid,
        isHighestBidder: Boolean
    ): BidDtos.CancelPolicy {
        val now = LocalDateTime.now()
        val bidAge = Duration.between(bid.bidTime, now)
        val timeUntilEnd = Duration.between(now, auction.endTime)

        // 경매 종료 10분 전 -> 경매 포기 불가
        if (timeUntilEnd.toMinutes() < NO_CANCEL_BEFORE_END_MINUTES) {
            throw BusinessException(ErrorCode.CANCEL_NOT_ALLOWED, "경매 종료 10분 전에는 입찰을 포기할 수 없습니다.")
        }

        // 최고 입찰자가 아닌 경우 -> 입찰 포기(Free)
        if (!isHighestBidder) {
            return BidDtos.CancelPolicy(
                allowed = true,
                fee = 0L,
                message = "무료 포기"
            )
        }

        // 최고 입찰자인 경우
        return if (bidAge.toMinutes() < FREE_CANCEL_MINUTES) {
            // 5분 이내 -> 입찰 포기(Free)
            BidDtos.CancelPolicy(
                allowed = true,
                fee = 0L,
                message = "입찰 후 5분 이내 무료 포기"
            )
        } else {
            // 5분 경과: 1% 수수료
            val fee = (bid.amount * CANCEL_FEE_RATE).toLong()

            BidDtos.CancelPolicy(
                allowed = true,
                fee = fee,
                message = "입찰가의 1% 수수료 부과"
            )
        }
    }

    /**
     * 이전 최고 입찰가로 롤백
     */
    private fun rollbackToNextHighestBid(
        auctionId: Long,
        cancelledUserId: Long
    ): Long {
        // 취소된 입찰을 제외한 활성 입찰 중 최고가 찾기
        val activeBids = bidJpaRepository.findByAuctionIdAndStatusOrderByAmountDesc(
            auctionId = auctionId,
            status = BidStatus.ACTIVE
        )

        val nextHighestBid = activeBids.firstOrNull { it.userId != cancelledUserId }

        return if (nextHighestBid != null) {
            // Redis 업데이트
            auctionRepository.updateCurrentPrice(
                auctionId = auctionId,
                newPrice = nextHighestBid.amount,
                newBidderId = nextHighestBid.userId,
                newBidTime = nextHighestBid.bidTime
            )

            logger.info("Rolled back to next highest bid: auctionId=$auctionId, price=${nextHighestBid.amount}, bidderId=${nextHighestBid.userId}")

            nextHighestBid.amount
        } else {
            // 다른 입찰이 없으면 시작가로 롤백
            val auction = auctionJpaRepository.findById(auctionId).orElseThrow()
            auctionRepository.updateCurrentPrice(
                auctionId = auctionId,
                newPrice = auction.startPrice,
                newBidderId = null,
                newBidTime = null
            )

            logger.info("Rolled back to start price: auctionId=$auctionId, price=${auction.startPrice}")

            auction.startPrice
        }
    }
}

