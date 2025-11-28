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
        val auction = Auction.create(
            title = request.title,
            startPrice = request.startPrice,
            startTime = request.startTime,
            endTime = request.endTime,
            sellerId = 0L // 관리자 생성
        )
        
        // 관리자 생성은 바로 승인 상태로? 혹은 READY 상태로?
        // Auction 기본값이 PENDING이므로, 관리자가 생성하면 바로 승인 처리하거나 READY 상태로 변경 필요할 수도 있음.
        // 여기서는 PENDING으로 생성하고 별도 승인이 필요하다고 가정하거나, 
        // 편의상 approve() 호출하여 APPROVED 상태로 만들 수도 있음.
        // 일단 PENDING 상태로 둠.
        
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
            
        // PENDING, READY, APPROVED 상태에서 삭제 가능하도록 수정 필요
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
    fun approveAuction(id: Long) {
        val auction = auctionJpaRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }
        auction.approve()
    }

    @Transactional
    fun rejectAuction(id: Long, reason: String) {
        val auction = auctionJpaRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }
        auction.reject(reason)
    }
}
