package com.ch.auction.application.scheduler

import com.ch.auction.application.service.PaymentService
import com.ch.auction.domain.AuctionStatus
import com.ch.auction.domain.repository.AuctionRepository
import com.ch.auction.infrastructure.persistence.AuctionJpaRepository
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class AuctionEndScheduler(
    private val auctionJpaRepository: AuctionJpaRepository,
    private val auctionRepository: AuctionRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val paymentService: PaymentService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 */1 * * * ?")
    @SchedulerLock(
        name = "AuctionEndScheduler_endScheduledAuctions",
        lockAtLeastFor = "PT5S",
        lockAtMostFor = "PT50S"
    )
    @Transactional
    fun endScheduledAuctions() {
        val auctions = auctionJpaRepository.findAllByStatusAndEndTimeLessThanEqual(
            status = AuctionStatus.ONGOING,
            now = LocalDateTime.now()
        )

        for (auction in auctions) {
            logger.info("Ending auction: {}", auction.id)

            auction.closeAuction()
            
            val redisInfo = auctionRepository.getAuctionRedisInfo(
                auctionId = auction.id!!
            )
            
            if (redisInfo != null && redisInfo.lastBidderId != null) {

                auction.completeAuction()
                
                // 정산 (포인트 차감)
                try {
                    paymentService.settleAuction(
                        userId = redisInfo.lastBidderId,
                        amount = redisInfo.currentPrice.toBigDecimal()
                    )
                } catch (e: Exception) {
                    logger.error("Failed to settle auction {}: {}", auction.id, e.message)
                    // 실패 처리 (롤백 등 필요)
                    // 여기서는 로그만 남기고 진행하지만, 실제로는 재시도 혹은 상태 변경 필요
                }

                val message = mapOf(
                    "type" to AuctionStatus.COMPLETED.name,
                    "auctionId" to auction.id,
                    "winnerId" to redisInfo.lastBidderId,
                    "finalPrice" to redisInfo.currentPrice
                )

                messagingTemplate.convertAndSend("/topic/auctions/${auction.id}", message)

                logger.info("Auction {} completed. Winner: {}, Price: {}", auction.id, redisInfo.lastBidderId, redisInfo.currentPrice)
            } else {
                auction.failAuction()
                
                val message = mapOf(
                    "type" to AuctionStatus.FAILED.name,
                    "auctionId" to auction.id
                )

                messagingTemplate.convertAndSend("/topic/auctions/${auction.id}", message)

                logger.info("Auction {} failed (no bidders).", auction.id)
            }
            
            auctionRepository.expireAuctionRedisInfo(
                auctionId = auction.id,
                seconds = 3600
            )
        }
    }
}
