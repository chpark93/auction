package com.ch.auction.application.service

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.domain.Auction
import com.ch.auction.domain.AuctionStatus
import com.ch.auction.domain.repository.AuctionRepository
import com.ch.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.interfaces.api.dto.admin.AuctionAdminResponse
import com.ch.auction.interfaces.api.dto.admin.AuctionCreateRequest
import com.ch.auction.interfaces.api.dto.admin.AuctionUpdateRequest
import com.ch.auction.interfaces.common.ErrorCode
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
        val auction = Auction(
            title = request.title,
            startPrice = request.startPrice,
            startTime = request.startTime,
            endTime = request.endTime
        )
        val saved = auctionJpaRepository.save(auction)
        
        return AuctionAdminResponse.from(saved)
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

        return AuctionAdminResponse.from(auction)
    }

    @Transactional
    fun deleteAuction(
        id: Long
    ) {
        val auction = auctionJpaRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }
            
        if (auction.status != AuctionStatus.READY) {
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
}
