package com.ch.auction.gateway.common

import org.springframework.http.HttpStatus

data class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    companion object {
        val TOKEN_INVALID = ErrorCode(HttpStatus.UNAUTHORIZED, "J001", "유효하지 않은 JWT 토큰입니다.")
        val TOKEN_EXPIRED = ErrorCode(HttpStatus.UNAUTHORIZED, "J002", "만료된 JWT 토큰입니다.")
        val TOKEN_UNSUPPORTED = ErrorCode(HttpStatus.UNAUTHORIZED, "J003", "지원하지 않는 JWT 토큰입니다.")
        val TOKEN_EMPTY = ErrorCode(HttpStatus.UNAUTHORIZED, "J004", "JWT 클레임이 비어 있습니다.")
        val TOKEN_WRONG_TYPE = ErrorCode(HttpStatus.UNAUTHORIZED, "J005", "잘못된 타입의 JWT 토큰입니다.")
        val TOKEN_NOT_FOUND = ErrorCode(HttpStatus.UNAUTHORIZED, "J006", "JWT 토큰을 찾을 수 없습니다.")
        val FORBIDDEN = ErrorCode(HttpStatus.FORBIDDEN, "J007", "접근 권한이 없습니다.")

        val RATE_LIMIT_EXCEEDED = ErrorCode(HttpStatus.TOO_MANY_REQUESTS, "R001", "요청 횟수 제한을 초과했습니다. 잠시 후 다시 시도해주세요.")
    }
}

