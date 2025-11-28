package com.ch.auction.interfaces.common

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid Input Value"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "Internal Server Error"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "Resource not found"),
    
    // Auction
    PRICE_TOO_LOW(HttpStatus.BAD_REQUEST, "A001", "Bid amount must be higher than current price"),
    AUCTION_ENDED(HttpStatus.BAD_REQUEST, "A002", "Auction has already ended"),
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "A003", "Auction not found"),
    AUCTION_NOT_ONGOING(HttpStatus.BAD_REQUEST, "A004", "Auction is not ongoing"),
    ACTION_NOT_READY(HttpStatus.BAD_REQUEST, "A005", "Auction is not ready to start"),
    AUCTION_ID_MUST_NOT_NULL(HttpStatus.BAD_REQUEST, "A006", "Auction ID must not be null"),
    AUCTION_NOT_READY(HttpStatus.BAD_REQUEST, "A007", "Auction has not started yet"),
    AUCTION_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "A008", "Auction has already ended"),
    AUCTION_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "A009", "Auction has already started"),
    AUCTION_NOT_ENDED(HttpStatus.BAD_REQUEST, "A010", "Auction is not ended"),
    AUCTION_NOT_PENDING(HttpStatus.BAD_REQUEST, "A011", "Auction is not pending approval"),

    // Bid
    BID_FAILED(HttpStatus.BAD_REQUEST, "B001", "Bid failed"),
    BID_AMOUNT_HIGHER_THAN_CURRENT_PRICE(HttpStatus.BAD_REQUEST, "B002", "Bid amount must be higher than current price"),
    SELF_BIDDING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "B003", "Self bidding is not allowed"),
    NOT_ENOUGH_POINT(HttpStatus.BAD_REQUEST, "B004", "Not enough point"),
    OUTBIDDED(HttpStatus.BAD_REQUEST, "B005", "Outbidded by auto bidder"),

    // Lua Script Errors
    UNEXPECTED_STATE_LUA_SCRIPT(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "Unexpected result from Lua script")
}

