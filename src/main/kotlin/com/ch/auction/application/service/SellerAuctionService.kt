package com.ch.auction.application.service

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.domain.Auction
import com.ch.auction.domain.AuctionStatus
import com.ch.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.interfaces.api.dto.admin.AuctionCreateRequest
import com.ch.auction.interfaces.common.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SellerAuctionService(
    private val auctionJpaRepository: AuctionJpaRepository
) {
    @Transactional
    fun createAuction(
        sellerId: Long,
        request: AuctionCreateRequest
    ): Long {
        val auction = Auction(
            title = request.title,
            startPrice = request.startPrice,
            startTime = request.startTime,
            endTime = request.endTime,
            sellerId = sellerId
        )

        return auctionJpaRepository.save(auction).id!!
    }

    @Transactional
    fun deleteAuction(
        sellerId: Long,
        auctionId: Long
    ) {
        val auction = auctionJpaRepository.findById(auctionId)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }
            
        if (auction.sellerId != sellerId) {
            throw IllegalArgumentException("Not owner of this auction")
        }
        
        if (auction.status != AuctionStatus.PENDING) {
             throw BusinessException(ErrorCode.AUCTION_ALREADY_STARTED)
        }
        
        auction.delete()
    }
}

