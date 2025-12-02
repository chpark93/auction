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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuctionAdminService(
    private val auctionJpaRepository: AuctionJpaRepository,
    private val auctionRepository: AuctionRepository
) {

    @Transactional
    fun createAuction(
        request: AuctionCreateRequest
    ): AuctionAdminResponse {
        val auction = Auction.create(
            title = request.title,
            startPrice = request.startPrice,
            startTime = request.startTime,
            endTime = request.endTime,
            sellerId = 0L // TODO: Admin 생성 시 Seller 지정 로직 필요 시 수정
        )
        
        val savedAuction = auctionJpaRepository.save(auction)
        
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

        return AuctionAdminResponse.from(
            auction = auction
        )
    }

    @Transactional
    fun deleteAuction(
        id: Long
    ) {
        val auction = auctionJpaRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }
            
        if (auction.status != AuctionStatus.PENDING && 
            auction.status != AuctionStatus.READY &&
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
