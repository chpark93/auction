package com.ch.auction.auction.application.scheduler

import com.ch.auction.auction.domain.AuctionRepository
import com.ch.auction.auction.domain.AuctionStatus
import com.ch.auction.auction.infrastructure.persistence.AuctionJpaRepository
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class AuctionStartScheduler(
    private val auctionJpaRepository: AuctionJpaRepository,
    private val auctionRepository: AuctionRepository,
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 */1 * * * ?")
    @SchedulerLock(
        name = "AuctionStartScheduler",
        lockAtLeastFor = "PT5S",
        lockAtMostFor = "PT50S"
    )
    @Transactional
    fun startScheduledAuctions() {
        val auctions = auctionJpaRepository.findAllByStatusAndStartTimeLessThanEqual(
            status = AuctionStatus.READY,
            now = LocalDateTime.now()
        )

        auctions.forEach { auction ->
            logger.info("Starting auction: {}", auction.id)

            auction.startAuction()

            auctionRepository.loadAuctionToRedis(
                auctionId = auction.id!!
            )

            val message = mapOf(
                "type" to AuctionStatus.ONGOING.name,
                "auctionId" to auction.id,
                "startPrice" to auction.startPrice
            )

            messagingTemplate.convertAndSend("/topic/auctions/${auction.id}", message)
        }
    }
}
