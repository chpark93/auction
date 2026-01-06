package com.ch.auction.admin.common

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
    USER_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "User service is currently unavailable"),

    // Auction
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Auction not found"),
    
    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Invalid Token"),
    ;

    val code: String
        get() = this.name
}
