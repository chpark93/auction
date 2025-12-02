package com.ch.auction.gateway.common

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "J001", "유효하지 않은 JWT 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "J002", "만료된 JWT 토큰입니다."),
    TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "J003", "지원하지 않는 JWT 토큰입니다."),
    TOKEN_EMPTY(HttpStatus.UNAUTHORIZED, "J004", "JWT 클레임이 비어 있습니다."),
    TOKEN_WRONG_TYPE(HttpStatus.UNAUTHORIZED, "J005", "잘못된 타입의 JWT 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "J006", "JWT 토큰을 찾을 수 없습니다.")
}

