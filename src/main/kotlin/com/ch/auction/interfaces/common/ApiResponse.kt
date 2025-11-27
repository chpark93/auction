package com.ch.auction.interfaces.common

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
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
        fun <T> ok(
            data: T
        ): ApiResponse<T> = ApiResponse(
            success = true,
            data = data
        )

        fun ok(): ApiResponse<Unit> = ApiResponse(
            success = true
        )

        fun fail(
            code: String,
            message: String
        ): ApiResponse<Unit> = ApiResponse(
            success = false,
            error = Error(
                code = code,
                message = message
            )
        )
        
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
