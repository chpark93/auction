package com.ch.auction.gateway.common

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
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded"),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Invalid Token"),
    TOKEN_WRONG_TYPE(HttpStatus.UNAUTHORIZED, "Wrong Type Token"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Expired Token"),
    TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "Unsupported Token"),
    TOKEN_EMPTY(HttpStatus.UNAUTHORIZED, "Empty Token"),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Token not found"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token"),
    ;

    val code: String
        get() = this.name
}
