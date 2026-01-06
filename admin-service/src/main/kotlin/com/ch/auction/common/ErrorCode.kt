package com.ch.auction.common

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val message: String
) {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "Invalid Input Value"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access is Denied"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    EMAIL_DUPLICATED(HttpStatus.BAD_REQUEST, "Email is already in use"),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "Password does not match"),
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
    AUCTION_NOT_OWNER(HttpStatus.FORBIDDEN, "You are not the owner of this auction"),
    ACTION_NOT_READY(HttpStatus.BAD_REQUEST, "Action is not ready"),
    BID_AMOUNT_HIGHER_THAN_CURRENT_PRICE(HttpStatus.BAD_REQUEST, "Bid amount must be higher than current price"),
    AUCTION_ID_MUST_NOT_NULL(HttpStatus.BAD_REQUEST, "Auction id must not null"),
    UNEXPECTED_STATE_LUA_SCRIPT(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected state in lua script"),

    // Bid
    PRICE_TOO_LOW(HttpStatus.BAD_REQUEST, "Price is too low"),
    AUCTION_ENDED(HttpStatus.BAD_REQUEST, "Auction is ended"),
    SELF_BIDDING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Self bidding is not allowed"),
    NOT_ENOUGH_POINT(HttpStatus.BAD_REQUEST, "Not enough point"),
    BID_NOT_FOUND(HttpStatus.NOT_FOUND, "Bid not found"),
    CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Cancel not allowed"),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Invalid Token"),
    TOKEN_WRONG_TYPE(HttpStatus.UNAUTHORIZED, "Wrong Type Token"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Expired Token"),
    TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "Unsupported Token"),
    TOKEN_EMPTY(HttpStatus.UNAUTHORIZED, "Empty Token"),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Token not found"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token"),

    // User
    ALREADY_VERIFIED_USER(HttpStatus.BAD_REQUEST, "User is already verified"),
    DUPLICATE_CI(HttpStatus.BAD_REQUEST, "CI is already in use"),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order not found"),
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "Delivery not found"),

    // Payment
    DUPLICATE_PAYMENT(HttpStatus.CONFLICT, "Duplicate payment"),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Payment not found"),
    PAYMENT_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "Payment verification failed"),

    // Settlement
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Settlement not found")
    ;

    val code: String
        get() = this.name
}
