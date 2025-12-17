package com.ch.auction.search.application.consumer

import com.ch.auction.search.domain.document.AuctionDocument
import com.ch.auction.search.infrastructure.client.product.ProductClient
import com.ch.auction.search.infrastructure.persistence.AuctionSearchRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class AuctionEventConsumer(
    private val auctionSearchRepository: AuctionSearchRepository,
    private val productClient: ProductClient,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 경매 생성 이벤트 처리
     */
    @KafkaListener(topics = ["auction-create-topic"], groupId = "search-service-group")
    fun handleAuctionCreated(message: String) {
        try {
            logger.info("Received auction-create event: $message")
            val event: Map<String, Any> = objectMapper.readValue(message)

            val auctionId = event["id"].toString()
            val productId = (event["productId"] as? Number)?.toLong()
                ?: throw IllegalArgumentException("productId is required")

            // Product 정보 조회
            val productResponse = productClient.getProduct(productId)
            val product = productResponse.data
                ?: throw IllegalArgumentException("Product not found: $productId")

            // AuctionDocument 생성
            val document = AuctionDocument(
                id = auctionId,
                productId = productId,
                title = product.title,
                description = product.description,
                category = product.category,
                condition = product.condition,
                sellerName = event["sellerName"] as? String ?: "Unknown",
                sellerId = product.sellerId,
                startPrice = (event["startPrice"] as Number).toLong(),
                currentPrice = (event["startPrice"] as Number).toLong(),
                bidCount = 0,
                status = event["status"] as? String ?: "PENDING",
                thumbnailUrl = product.thumbnailUrl,
                createdAt = parseDateTime(event["createdAt"] as String),
                endTime = parseDateTime(event["endTime"] as String)
            )

            auctionSearchRepository.save(document)
            logger.info("Auction document created in ES: $auctionId")
        } catch (e: Exception) {
            logger.error("Failed to handle auction-create event", e)
        }
    }

    /**
     * 경매 업데이트 이벤트 처리
     */
    @KafkaListener(topics = ["auction-update-topic"], groupId = "search-service-group")
    fun handleAuctionUpdated(message: String) {
        try {
            logger.info("Received auction-update event: $message")
            val event: Map<String, Any> = objectMapper.readValue(message)

            val auctionId = event["id"].toString()
            val existingDoc = auctionSearchRepository.findById(auctionId).orElse(null)

            if (existingDoc == null) {
                logger.warn("Auction document not found in ES: $auctionId")
                return
            }

            val updatedDoc = existingDoc.copy(
                currentPrice = (event["currentPrice"] as? Number)?.toLong() ?: existingDoc.currentPrice,
                bidCount = (event["bidCount"] as? Number)?.toInt() ?: existingDoc.bidCount,
                status = event["status"] as? String ?: existingDoc.status
            )

            auctionSearchRepository.save(updatedDoc)
            logger.info("Auction document updated in ES: $auctionId")
        } catch (e: Exception) {
            logger.error("Failed to handle auction-update event", e)
        }
    }

    /**
     * 입찰 성공 이벤트 처리
     */
    @KafkaListener(topics = ["bid-success-topic"], groupId = "search-service-group")
    fun handleBidSuccess(
        message: String
    ) {
        try {
            logger.info("Received bid-success event: $message")
            val event: Map<String, Any> = objectMapper.readValue(message)

            val auctionId = event["auctionId"].toString()
            val existingDoc = auctionSearchRepository.findById(auctionId).orElse(null)

            if (existingDoc == null) {
                logger.warn("Auction document not found in ES: $auctionId")
                return
            }

            val updatedDoc = existingDoc.copy(
                currentPrice = (event["currentPrice"] as Number).toLong(),
                bidCount = (event["bidCount"] as? Number)?.toInt() ?: (existingDoc.bidCount + 1)
            )

            auctionSearchRepository.save(updatedDoc)
            logger.info("Auction document updated after bid: $auctionId")
        } catch (e: Exception) {
            logger.error("Failed to handle bid-success event", e)
        }
    }

    /**
     * Product 업데이트 이벤트 처리
     */
    @KafkaListener(topics = ["product-update-topic"], groupId = "search-service-group")
    fun handleProductUpdated(
        message: String
    ) {
        try {
            logger.info("Received product-update event: $message")
            val event: Map<String, Any> = objectMapper.readValue(message)

            val productId = (event["productId"] as Number).toLong()

            val auctions = auctionSearchRepository.findByProductId(productId)

            if (auctions.isEmpty()) {
                logger.info("No auctions found for product: $productId")
                return
            }

            // Product 정보 재조회
            val productResponse = productClient.getProduct(productId)
            val product = productResponse.data
                ?: throw IllegalArgumentException("Product not found: $productId")

            // Auction Document 업데이트
            auctions.forEach { auction ->
                val updatedDoc = auction.copy(
                    title = product.title,
                    description = product.description,
                    category = product.category,
                    condition = product.condition,
                    thumbnailUrl = product.thumbnailUrl
                )
                auctionSearchRepository.save(updatedDoc)
                logger.info("Auction document updated after product change: ${auction.id}")
            }
        } catch (e: Exception) {
            logger.error("Failed to handle product-update event", e)
        }
    }

    private fun parseDateTime(
        dateTimeString: String
    ): LocalDateTime {
        return try {
            LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            logger.warn("Failed to parse datetime: $dateTimeString, using now()")
            LocalDateTime.now()
        }
    }
}

