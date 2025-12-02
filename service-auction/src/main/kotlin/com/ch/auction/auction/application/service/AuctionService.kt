package com.ch.auction.auction.application.service

import com.ch.auction.auction.domain.AuctionRepository
import com.ch.auction.auction.domain.BidResult
import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AuctionService(
    private val auctionRepository: AuctionRepository
) {
    /**
     * 입찰
     */
    fun placeBid(
        auctionId: Long,
        userId: Long,
        amount: Long
    ): Long {
        val requestTime = Instant.now().toEpochMilli()

        val result = auctionRepository.tryBid(
            auctionId = auctionId,
            userId = userId,
            amount = amount,
            requestTime = requestTime
        )

        return when (result) {
            is BidResult.Success -> result.currentPrice
            is BidResult.PriceTooLow -> throw BusinessException(ErrorCode.PRICE_TOO_LOW)
            is BidResult.AuctionEnded -> throw BusinessException(ErrorCode.AUCTION_ENDED)
            is BidResult.AuctionNotFound -> throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)
            is BidResult.SelfBidding -> throw BusinessException(ErrorCode.SELF_BIDDING_NOT_ALLOWED)
            is BidResult.NotEnoughPoint -> throw BusinessException(ErrorCode.NOT_ENOUGH_POINT)
        }
    }

    fun getAuctionCurrentPrice(
        auctionId: Long
    ): Long {
        val info = auctionRepository.getAuctionRedisInfo(
            auctionId = auctionId
        ) ?: throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)

        return info.currentPrice
    }
}
