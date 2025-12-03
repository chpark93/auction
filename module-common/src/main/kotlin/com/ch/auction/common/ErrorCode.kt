package com.ch.auction.common

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val message: String
) {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "Invalid Input Value"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "Entity Not Found"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access is Denied"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST, "Email is already in use"),
    POINT_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "Point is not enough"),
    USER_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "User service is currently unavailable"),
    USER_NOT_ACTIVE(HttpStatus.FORBIDDEN, "User is not active"),

    // Auction
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Auction not found"),
    AUCTION_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "Auction already started"),
    AUCTION_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "Auction already ended"),
    AUCTION_NOT_ONGOING(HttpStatus.BAD_REQUEST, "Auction is not ongoing"),
    AUCTION_NOT_PENDING(HttpStatus.BAD_REQUEST, "Auction is not pending"),
    AUCTION_NOT_ENDED(HttpStatus.BAD_REQUEST, "Auction is not ended"),
    AUCTION_NOT_READY(HttpStatus.BAD_REQUEST, "Auction is not ready"),
    ACTION_NOT_READY(HttpStatus.BAD_REQUEST, "Action is not ready"),
    BID_AMOUNT_HIGHER_THAN_CURRENT_PRICE(HttpStatus.BAD_REQUEST, "Bid amount must be higher than current price"),
    AUCTION_ID_MUST_NOT_NULL(HttpStatus.INTERNAL_SERVER_ERROR, "Auction id must not null"),

    // Bid
    PRICE_TOO_LOW(HttpStatus.BAD_REQUEST, "Price is too low"),
    AUCTION_ENDED(HttpStatus.BAD_REQUEST, "Auction is ended"),
    SELF_BIDDING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Self bidding is not allowed"),
    NOT_ENOUGH_POINT(HttpStatus.BAD_REQUEST, "Not enough point"),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid Token"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "Expired Token")
}
