package com.ch.auction.application.service

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.domain.repository.AuctionRepository
import com.ch.auction.domain.repository.BidResult
import com.ch.auction.interfaces.common.ErrorCode
import org.springframework.stereotype.Service
import java.math.BigDecimal
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
        amount: BigDecimal
    ): BigDecimal {
        val requestTime = Instant.now().toEpochMilli()
        val result = auctionRepository.tryBid(
            auctionId = auctionId,
            userId = userId,
            amount = amount,
            requestTime = requestTime
        )

        return when (result) {
            is BidResult.Success -> result.newPrice
            is BidResult.PriceTooLow -> throw BusinessException(ErrorCode.PRICE_TOO_LOW)
            is BidResult.AuctionEnded -> throw BusinessException(ErrorCode.AUCTION_ENDED)
            is BidResult.AuctionNotFound -> throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)
            is BidResult.SelfBidding -> throw BusinessException(ErrorCode.SELF_BIDDING_NOT_ALLOWED)
            is BidResult.NotEnoughPoint -> throw BusinessException(ErrorCode.NOT_ENOUGH_POINT)
        }
    }

    fun getAuctionCurrentPrice(auctionId: Long): BigDecimal {
        val info = auctionRepository.getAuctionRedisInfo(auctionId)
            ?: throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)
        return BigDecimal.valueOf(info.currentPrice)
    }
}
