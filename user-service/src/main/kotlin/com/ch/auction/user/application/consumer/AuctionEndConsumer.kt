package com.ch.auction.user.application.consumer

import com.ch.auction.common.event.AuctionEndedEvent
import com.ch.auction.user.application.service.PointTransactionService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class AuctionEndConsumer(
    private val pointTransactionService: PointTransactionService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["auction-ended"],
        groupId = "user-service-group"
    )
    fun handleAuctionEnded(
        event: AuctionEndedEvent
    ) {
        logger.info("Received auction ended event: auctionId=${event.auctionId}, winnerId=${event.winnerId}")
        
        try {
            val winnerId = event.winnerId
            if (winnerId != null) {
                // 낙찰 - 낙찰자는 USE, 나머지는 RELEASE
                pointTransactionService.processAuctionCompleted(
                    auctionId = event.auctionId,
                    winnerId = winnerId,
                    finalPrice = event.finalPrice
                )
            } else {
                // 유찰 - 모든 입찰자 RELEASE
                pointTransactionService.processAuctionFailed(
                    auctionId = event.auctionId
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to process point transactions for auction ${event.auctionId}", e)
        }
    }
}

