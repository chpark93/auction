package com.ch.auction.auction.infrastructure.event

import com.ch.auction.auction.domain.event.BidSuccessEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class BidEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @EventListener
    fun handleBidSuccess(
        event: BidSuccessEvent
    ) {
        try {
            val kafkaEvent = mapOf(
                "auctionId" to event.auctionId,
                "userId" to event.userId,
                "currentPrice" to event.amount,
                "bidCount" to event.sequence.toInt()
            )
            
            val eventJson = objectMapper.writeValueAsString(kafkaEvent)
            
            kafkaTemplate.send("bid-success-topic", eventJson)
            logger.info("Published bid-success event to Kafka for auction ${event.auctionId}")
        } catch (e: Exception) {
            logger.error("Failed to publish bid-success event to Kafka", e)
        }
    }
}

