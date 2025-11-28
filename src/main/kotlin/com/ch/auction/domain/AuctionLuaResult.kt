package com.ch.auction.domain

enum class AuctionLuaResult(
    val code : String,
) {
    PRICE_TOO_LOW("0"),
    AUCTION_NOT_FOUND("-1"),
    AUCTION_ENDED("-2"),
    SELF_BIDDING("-3"),
    NOT_ENOUGH_POINT("-4"),
    OUTBID("-5")
    ;

    companion object {
        fun findByCode(
            code : String
        ) : AuctionLuaResult {

            return AuctionLuaResult.entries.first {
                it.code == code
            }
        }
    }
}

