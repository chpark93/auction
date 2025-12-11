package com.ch.auction.search.application.consumer

import com.ch.auction.common.event.AuctionEndedEvent
import com.ch.auction.search.application.dto.AuctionSearchDtos
import com.ch.auction.search.domain.document.AuctionDocument
import com.ch.auction.search.infrastructure.persistence.AuctionSearchRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.document.Document
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.ScriptType
import org.springframework.data.elasticsearch.core.query.UpdateQuery
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AuctionSyncConsumer(
    private val auctionSearchRepository: AuctionSearchRepository,
    private val elasticsearchOperations: ElasticsearchOperations,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["auction-create-topic"],
        groupId = "search-service-group"
    )
    fun handleAuctionCreate(
        message: String
    ) {
        try {
            val event = objectMapper.readValue(message, AuctionSearchDtos.AuctionCreateEvent::class.java)
            val document = AuctionDocument(
                id = event.id.toString(),
                title = event.title,
                category = event.category,
                sellerName = event.sellerName,
                startPrice = event.startPrice,
                currentPrice = event.startPrice,
                bidCount = 0,
                status = "PENDING",
                thumbnailUrl = event.thumbnailUrl,
                createdAt = LocalDateTime.parse(event.createdAt),
                endTime = LocalDateTime.parse(event.endTime)
            )
            auctionSearchRepository.save(document)

            logger.info("Indexed new auction: ${event.id}")
        } catch (e: Exception) {
            logger.error("Failed to process auction-create event", e)
        }
    }

    @KafkaListener(
        topics = ["auction-update-topic"],
        groupId = "search-service-group"
    )
    fun handleAuctionUpdate(
        message: String
    ) {
        try {
            val event = objectMapper.readValue(message, AuctionSearchDtos.AuctionUpdateEvent::class.java)
            val auctionId = event.id.toString()
            
            val existingDocument = auctionSearchRepository.findById(auctionId).orElse(null) ?: return

            val updatedDocument = existingDocument.copy(
                title = event.title,
                category = event.category,
                endTime = LocalDateTime.parse(event.endTime)
            )
            auctionSearchRepository.save(updatedDocument)

            logger.info("Updated auction index: ${event.id}")
        } catch (e: Exception) {
            logger.error("Failed to process auction-update event", e)
        }
    }

    @KafkaListener(
        topics = ["bid-success-topic"],
        groupId = "search-service-group"
    )
    fun handleBidSuccess(
        message: String
    ) {
        try {
            val event = objectMapper.readValue(message, AuctionSearchDtos.BidSuccessEvent::class.java)
            val auctionId = event.auctionId.toString()
            
            val existingDocument = auctionSearchRepository.findById(auctionId).orElse(null)
            if (existingDocument == null) {
                logger.warn("Auction document not found: $auctionId. Skipping update.")
                return
            }
            
            val updateQuery = UpdateQuery.builder(auctionId)
                .withScriptType(ScriptType.INLINE)
                .withScript("ctx._source.currentPrice = params.currentPrice; ctx._source.bidCount = params.bidCount")
                .withParams(mapOf(
                    "currentPrice" to event.currentPrice,
                    "bidCount" to event.bidCount
                ))
                .build()

            elasticsearchOperations.update(updateQuery, IndexCoordinates.of("auctions"))

            logger.info("Updated price for auction: ${event.auctionId}")
        } catch (e: Exception) {
            logger.error("Failed to process bid-success event", e)
        }
    }

    @KafkaListener(
        topics = ["auction-ended"],
        groupId = "search-service-group"
    )
    fun handleAuctionEnd(
        event: AuctionEndedEvent
    ) {
        try {
            val auctionId = event.auctionId.toString()
            
            val existingDocument = auctionSearchRepository.findById(auctionId).orElse(null)
            if (existingDocument == null) {
                logger.warn("Auction document not found: $auctionId. Skipping update.")
                return
            }
            
            val status = if (event.winnerId != null) "COMPLETED" else "FAILED"
            
            val updateQuery = UpdateQuery.builder(auctionId)
                .withDocument(Document.create().apply {
                    put("status", status)
                    put("currentPrice", event.finalPrice)
                })
                .build()

            elasticsearchOperations.update(updateQuery, IndexCoordinates.of("auctions"))

            logger.info("Auction ended: ${event.auctionId}, Status: $status")
        } catch (e: Exception) {
            logger.error("Failed to process auction-ended event", e)
        }
    }
}

