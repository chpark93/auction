package com.ch.auction.domain.repository

import java.math.BigDecimal

sealed class BidResult {

    data class Success(
        val newPrice: BigDecimal
    ) : BidResult()

    object PriceTooLow : BidResult()
    object AuctionEnded : BidResult()
    object AuctionNotFound : BidResult()
    object SelfBidding : BidResult()
    object NotEnoughPoint : BidResult()
    object Outbidded : BidResult()
}
