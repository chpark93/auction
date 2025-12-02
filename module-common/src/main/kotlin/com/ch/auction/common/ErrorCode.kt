package com.ch.auction.common

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
    FORBIDDEN(HttpStatus.FORBIDDEN, "C004", "Forbidden"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C005", "인증이 필요합니다."),

    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "J001", "유효하지 않은 JWT 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "J002", "만료된 JWT 토큰입니다."),
    TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "J003", "지원하지 않는 JWT 토큰입니다."),
    TOKEN_EMPTY(HttpStatus.UNAUTHORIZED, "J004", "JWT 클레임이 비어 있습니다."),
    TOKEN_WRONG_TYPE(HttpStatus.UNAUTHORIZED, "J005", "잘못된 타입의 JWT 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "J006", "JWT 토큰을 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "J007", "유효하지 않은 리프레시 토큰입니다."),

    // Auth
    EMAIL_DUPLICATED(HttpStatus.BAD_REQUEST, "U001", "Email is already in use"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "User not found"),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "U003", "Password mismatch"),
    ALREADY_VERIFIED_USER(HttpStatus.BAD_REQUEST, "U004", "User is already verified"),
    DUPLICATE_CI(HttpStatus.BAD_REQUEST, "U005", "User with this CI already exists"),

    // Payment
    PAYMENT_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "M001", "Payment verification failed"),
    DUPLICATE_PAYMENT(HttpStatus.BAD_REQUEST, "M002", "Duplicate payment"),

    // Order & Delivery
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "Order not found"),
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "O002", "Delivery not found"),

    // Auction
    PRICE_TOO_LOW(HttpStatus.BAD_REQUEST, "A001", "Bid amount must be higher than current price"),
    AUCTION_ENDED(HttpStatus.BAD_REQUEST, "A002", "Auction has already ended"),
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "A003", "Auction not found"),
    AUCTION_NOT_ONGOING(HttpStatus.BAD_REQUEST, "A004", "Auction is not ongoing"),
    ACTION_NOT_READY(HttpStatus.BAD_REQUEST, "A005", "Auction is not ready to start"),
    AUCTION_ID_MUST_NOT_NULL(HttpStatus.BAD_REQUEST, "A006", "Auction ID must not be null"),
    AUCTION_NOT_READY(HttpStatus.BAD_REQUEST, "A007", "Auction has not started yet"),
    AUCTION_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "A008", "Auction has already ended"),
    AUCTION_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "A009", "Auction has already started or is not in a modifiable state"),
    AUCTION_NOT_ENDED(HttpStatus.BAD_REQUEST, "A010", "Auction has not ended yet"),
    AUCTION_NOT_PENDING(HttpStatus.BAD_REQUEST, "A011", "Auction is not in PENDING status"),
    AUCTION_NOT_OWNER(HttpStatus.FORBIDDEN, "A012", "You are not the owner of this auction"),

    // Bid
    BID_FAILED(HttpStatus.BAD_REQUEST, "B001", "Bid failed"),
    BID_AMOUNT_HIGHER_THAN_CURRENT_PRICE(HttpStatus.BAD_REQUEST, "B002", "Bid amount must be higher than current price"),
    SELF_BIDDING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "B003", "Self-bidding is not allowed"),
    NOT_ENOUGH_POINT(HttpStatus.BAD_REQUEST, "B004", "Not enough point"),

    // Point
    POINT_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "P001", "Point is not enough"),

    // Lua Script Errors
    UNEXPECTED_STATE_LUA_SCRIPT(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "Unexpected result from Lua script")
}
