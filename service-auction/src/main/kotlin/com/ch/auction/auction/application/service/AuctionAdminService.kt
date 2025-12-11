package com.ch.auction.auction.application.service

import com.ch.auction.auction.domain.Auction
import com.ch.auction.auction.domain.AuctionRepository
import com.ch.auction.auction.domain.AuctionStatus
import com.ch.auction.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.auction.interfaces.api.dto.admin.AuctionAdminResponse
import com.ch.auction.auction.interfaces.api.dto.admin.AuctionCreateRequest
import com.ch.auction.auction.interfaces.api.dto.admin.AuctionUpdateRequest
import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
class AuctionAdminService(
    private val auctionJpaRepository: AuctionJpaRepository,
    private val auctionRepository: AuctionRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {

    @Transactional(readOnly = true)
    fun getAuctions(
        page: Int,
        size: Int,
        status: AuctionStatus?
    ): Page<AuctionAdminResponse> {
        val pageable = PageRequest.of(page, size)
        
        val auctions = if (status != null) {
            auctionJpaRepository.findAllByStatusAndDeletedFalse(status, pageable)
        } else {
            auctionJpaRepository.findAllByDeletedFalse(pageable)
        }
        
        return auctions.map { AuctionAdminResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getAuction(
        id: Long
    ): AuctionAdminResponse {
        val auction = auctionJpaRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }
        
        return AuctionAdminResponse.from(auction)
    }

    @Transactional
    fun createAuction(
        request: AuctionCreateRequest
    ): AuctionAdminResponse {
        val auction = Auction.create(
            title = request.title,
            startPrice = request.startPrice,
            startTime = request.startTime,
            endTime = request.endTime,
            sellerId = 0L // Admin 생성
        )
        
        // 관리자 직접 생성은 바로 승인 상태로
        auction.approve()
        
        val savedAuction = auctionJpaRepository.save(auction)
        
        // Kafka 이벤트 발행 (Elasticsearch 동기화용)
        publishAuctionCreateEvent(savedAuction)
        
        return AuctionAdminResponse.from(
            auction = savedAuction
        )
    }

    @Transactional
    fun updateAuction(
        id: Long,
        request: AuctionUpdateRequest
    ): AuctionAdminResponse {
        val auction = auctionJpaRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }

        auction.update(
            title = request.title,
            startPrice = request.startPrice,
            startTime = request.startTime,
            endTime = request.endTime
        )

        // Kafka 이벤트 발행 (Elasticsearch 동기화용)
        publishAuctionUpdateEvent(auction)

        return AuctionAdminResponse.from(
            auction = auction
        )
    }
    
    private fun publishAuctionCreateEvent(
        auction: Auction
    ) {
        val event = mapOf(
            "id" to auction.id!!,
            "title" to auction.title,
            "category" to "관리자등록",
            "sellerName" to "Admin",
            "startPrice" to auction.startPrice,
            "thumbnailUrl" to null,
            "createdAt" to auction.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "endTime" to auction.endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
        
        val eventJson = objectMapper.writeValueAsString(event)
        kafkaTemplate.send("auction-create-topic", eventJson)
    }
    
    private fun publishAuctionUpdateEvent(
        auction: Auction
    ) {
        val event = mapOf(
            "id" to auction.id!!,
            "title" to auction.title,
            "category" to "관리자등록",
            "endTime" to auction.endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
        
        val eventJson = objectMapper.writeValueAsString(event)
        kafkaTemplate.send("auction-update-topic", eventJson)
    }

    @Transactional
    fun deleteAuction(
        id: Long
    ) {
        val auction = auctionJpaRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }
            
        if (auction.status != AuctionStatus.PENDING && 
            auction.status != AuctionStatus.APPROVED) {

            throw BusinessException(ErrorCode.AUCTION_ALREADY_STARTED)
        }
        
        auction.delete()
    }
    
    @Transactional
    fun startAuction(
        id: Long
    ) {
        val auction = auctionJpaRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }
            
        auction.startAuction()
        
        auctionRepository.loadAuctionToRedis(
            auctionId = id
        )
    }
    
    @Transactional
    fun endAuction(
        id: Long
    ) {
        val auction = auctionJpaRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }
            
        auction.closeAuction()
    }

    @Transactional
    fun approveAuction(
        id: Long
    ) {
        val auction = auctionJpaRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }

        auction.approve()
    }

    @Transactional
    fun rejectAuction(
        id: Long,
        reason: String
    ) {
        val auction = auctionJpaRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }

        auction.reject(
            reason = reason
        )
    }
}
