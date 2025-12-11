package com.ch.auction.auction.infrastructure.event

import com.ch.auction.auction.domain.event.BidSuccessEvent
import com.ch.auction.auction.infrastructure.client.user.UserClient
import com.ch.auction.auction.infrastructure.sse.SseEmitterManager
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class BidNotificationListener(
    private val sseEmitterManager: SseEmitterManager,
    private val userClient: UserClient
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @EventListener
    fun handleBidSuccess(
        event: BidSuccessEvent
    ) {
        logger.info("Broadcasting bid success event for auction ${event.auctionId}")
        
        val userEmail = try {
            val response = userClient.getUserInfo(
                userId = event.userId
            )

            response.data?.email ?: "None"
        } catch (e: Exception) {
            logger.error("Failed to fetch user info for bid notification", e)

            "None"
        }
        
        val bidUpdate = mapOf(
            "auctionId" to event.auctionId,
            "currentPrice" to event.amount,
            "bidderId" to event.userId,
            "bidderEmail" to userEmail,
            "bidTime" to event.bidTime.toString(),
            "sequence" to event.sequence
        )
        
        sseEmitterManager.sendToAuction(
            auctionId = event.auctionId,
            eventName = "bid-update",
            data = bidUpdate
        )
    }
}
