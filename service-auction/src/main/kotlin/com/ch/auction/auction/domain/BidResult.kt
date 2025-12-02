package com.ch.auction.auction.domain

sealed class BidResult {
    data class Success(
        val currentPrice: Long
    ) : BidResult()

    object PriceTooLow : BidResult()
    object AuctionNotFound : BidResult()
    object AuctionEnded : BidResult()
    object SelfBidding : BidResult()
    object NotEnoughPoint : BidResult()
}
