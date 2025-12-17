package com.ch.auction.auction.infrastructure.config

import com.ch.auction.auction.domain.AuctionRepository
import com.ch.auction.auction.domain.Auction
import com.ch.auction.auction.infrastructure.persistence.AuctionJpaRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class TestDataInitializer(
    private val auctionJpaRepository: AuctionJpaRepository,
    private val auctionRepository: AuctionRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(
        args: ApplicationArguments?
    ) {
        if (auctionJpaRepository.count() == 0L) {
            logger.info("Initializing test data...")
            
            val auction = Auction.create(
                productId = 1L,
                title = "Test Auction Item",
                thumbnailUrl = null,
                startPrice = 1000L,
                startTime = LocalDateTime.now().minusMinutes(1),
                endTime = LocalDateTime.now().plusDays(1),
                sellerId = 9999L
            )

            auction.approve()
            auction.startAuction()
            
            val savedAuction = auctionJpaRepository.save(auction)
            auctionRepository.loadAuctionToRedis(
                auctionId = savedAuction.id!!
            )

            // Kafka 이벤트 발행 (Elasticsearch 동기화용)
            val event = mapOf(
                "id" to savedAuction.id!!,
                "title" to savedAuction.title,
                "category" to "테스트", // 기본 카테고리
                "sellerName" to "TestSeller",
                "startPrice" to savedAuction.startPrice,
                "thumbnailUrl" to null,
                "createdAt" to savedAuction.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "endTime" to savedAuction.endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            
            val eventJson = objectMapper.writeValueAsString(event)
            kafkaTemplate.send("auction-create-topic", eventJson)
            logger.info("Published auction-create event to Kafka for auction ID: ${savedAuction.id}")

            logger.info("Test Auction created: ID=${savedAuction.id}, Price=${savedAuction.startPrice}")
        }
    }
}

