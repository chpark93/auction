package com.ch.auction.gateway.common

import java.time.LocalDateTime

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: Error? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    data class Error(
        val code: String,
        val message: String
    )

    companion object {
        fun fail(
            errorCode: ErrorCode
        ): ApiResponse<Unit> = ApiResponse(
            success = false,
            error = Error(
                code = errorCode.code,
                message = errorCode.message
            )
        )
    }
}

