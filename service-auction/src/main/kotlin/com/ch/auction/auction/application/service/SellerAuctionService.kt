package com.ch.auction.auction.application.service

import com.ch.auction.auction.domain.Auction
import com.ch.auction.auction.domain.AuctionStatus
import com.ch.auction.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.auction.interfaces.api.dto.admin.AuctionCreateRequest
import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
class SellerAuctionService(
    private val auctionJpaRepository: AuctionJpaRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {
    @Transactional
    fun createAuction(
        sellerId: Long,
        request: AuctionCreateRequest
    ): Long {
        val auction = Auction.create(
            title = request.title,
            startPrice = request.startPrice,
            startTime = request.startTime,
            endTime = request.endTime,
            sellerId = sellerId
        )

        val savedAuction = auctionJpaRepository.save(auction)
        
        // Kafka 이벤트 발행 (Elasticsearch 동기화용)
        val event = mapOf(
            "id" to savedAuction.id!!,
            "title" to savedAuction.title,
            "category" to "일반", // 기본 카테고리 (추후 확장 가능)
            "sellerName" to "Seller#$sellerId", // 추후 실제 판매자 닉네임으로 변경
            "startPrice" to savedAuction.startPrice,
            "thumbnailUrl" to null,
            "createdAt" to savedAuction.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "endTime" to savedAuction.endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
        
        val eventJson = objectMapper.writeValueAsString(event)
        kafkaTemplate.send("auction-create-topic", eventJson)

        return savedAuction.id!!
    }

    @Transactional
    fun deleteAuction(
        sellerId: Long,
        auctionId: Long
    ) {
        val auction = auctionJpaRepository.findById(auctionId)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }

        if (auction.sellerId != sellerId) {
            throw BusinessException(ErrorCode.AUCTION_NOT_OWNER)
        }

        if (auction.status != AuctionStatus.PENDING) {
             throw BusinessException(ErrorCode.AUCTION_ALREADY_STARTED)
        }

        auction.delete()
    }
}
