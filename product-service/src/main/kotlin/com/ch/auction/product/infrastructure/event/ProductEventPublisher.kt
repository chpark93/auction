package com.ch.auction.product.infrastructure.event

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class ProductEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Product 업데이트 이벤트 발행
     */
    fun publishProductUpdated(
        productId: Long
    ) {
        try {
            val event = mapOf(
                "productId" to productId,
                "timestamp" to System.currentTimeMillis()
            )
            val eventJson = objectMapper.writeValueAsString(event)
            kafkaTemplate.send("product-update-topic", eventJson)
            logger.info("Published product-update event for product: $productId")
        } catch (e: Exception) {
            logger.error("Failed to publish product-update event", e)
        }
    }
}

