package com.ch.auction.auction.domain

enum class AuctionLuaResult(
    val code : String,
) {
    PRICE_TOO_LOW("0"),
    AUCTION_NOT_FOUND("-1"),
    AUCTION_ENDED("-2"),
    SELF_BIDDING("-3"),
    NOT_ENOUGH_POINT("-4")
    ;

    companion object {
        fun findByCode(
            code : String
        ) : AuctionLuaResult {

            return entries.first {
                it.code == code
            }
        }
    }
}