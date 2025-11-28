package com.ch.auction.infrastructure.event

import com.ch.auction.domain.event.BidSuccessEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class BidNotificationListener(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {

    @Async
    @EventListener
    fun handleBidSuccess(
        event: BidSuccessEvent
    ) {
        val message = objectMapper.writeValueAsString(event)

        // Redis Message 발행 (auction-topic)
        redisTemplate.convertAndSend("auction-topic", message)
    }
}

