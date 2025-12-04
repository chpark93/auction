package com.ch.auction.auction.application.scheduler

import com.ch.auction.auction.domain.AuctionRepository
import com.ch.auction.auction.domain.AuctionStatus
import com.ch.auction.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.common.event.AuctionEndedEvent
import com.ch.auction.common.event.NotificationEvent
import com.ch.auction.common.event.NotificationType
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
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
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 */1 * * * ?")
    @SchedulerLock(
        name = "AuctionEndScheduler",
        lockAtLeastFor = "PT5S",
        lockAtMostFor = "PT50S"
    )
    @Transactional
    fun endScheduledAuctions() {
        val auctions = auctionJpaRepository.findAllByStatusAndEndTimeLessThanEqual(
            status = AuctionStatus.ONGOING,
            now = LocalDateTime.now()
        )

        auctions.forEach { auction ->
            logger.info("Ending auction: {}", auction.id)

            auction.closeAuction()

            val redisInfo = auctionRepository.getAuctionRedisInfo(
                auctionId = auction.id!!
            )

            if (redisInfo != null && redisInfo.lastBidderId != null) {

                auction.completeAuction()

                // 이벤트 발행 (정산 및 주문 생성)
                val event = AuctionEndedEvent(
                    auctionId = auction.id,
                    sellerId = auction.sellerId,
                    winnerId = redisInfo.lastBidderId,
                    finalPrice = redisInfo.currentPrice,
                    endedAt = LocalDateTime.now()
                )
                kafkaTemplate.send("auction-ended", event)

                // 알림 이벤트 발행 (낙찰자)
                kafkaTemplate.send("notification-send", NotificationEvent(
                    userId = redisInfo.lastBidderId,
                    message = "경매 '${auction.title}' 낙찰에 성공했습니다!",
                    type = NotificationType.BID_SUCCESS,
                    relatedId = auction.id
                ))
                
                // 알림 이벤트 발행 (판매자)
                kafkaTemplate.send("notification-send", NotificationEvent(
                    userId = auction.sellerId,
                    message = "경매 '${auction.title}'가 낙찰되었습니다.",
                    type = NotificationType.AUCTION_ENDED,
                    relatedId = auction.id
                ))

                val message = mapOf(
                    "type" to AuctionStatus.COMPLETED.name,
                    "auctionId" to auction.id,
                    "winnerId" to redisInfo.lastBidderId,
                    "payment" to redisInfo.currentPrice
                )

                messagingTemplate.convertAndSend("/topic/auctions/${auction.id}", message)

                logger.info("Auction {} completed. Winner: {}, Price: {}", auction.id, redisInfo.lastBidderId, redisInfo.currentPrice)
            } else {
                auction.failAuction()

                // 유찰 이벤트 발행
                val event = AuctionEndedEvent(
                    auctionId = auction.id,
                    sellerId = auction.sellerId,
                    winnerId = null,
                    finalPrice = 0L,
                    endedAt = LocalDateTime.now()
                )
                kafkaTemplate.send("auction-ended", event)
                
                // 알림 이벤트 발행 (판매자에게 유찰 알림)
                kafkaTemplate.send("notification-send", NotificationEvent(
                    userId = auction.sellerId,
                    message = "경매 '${auction.title}'가 유찰되었습니다.",
                    type = NotificationType.AUCTION_FAILED,
                    relatedId = auction.id
                ))

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