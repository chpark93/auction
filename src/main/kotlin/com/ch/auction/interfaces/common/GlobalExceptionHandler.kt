package com.ch.auction.interfaces.common

import com.ch.auction.application.exception.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        e: BusinessException
    ): ResponseEntity<ApiResponse<Unit>> {
        log.warn("BusinessException: {}", e.message)
        return ResponseEntity.status(e.errorCode.status)
            .body(ApiResponse.fail(e.errorCode))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        e: IllegalArgumentException
    ): ResponseEntity<ApiResponse<Unit>> {
        log.warn("IllegalArgumentException: {}", e.message)
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.status)
            .body(
                ApiResponse.fail(
                    message = e.message ?: ErrorCode.INVALID_INPUT_VALUE.message,
                    code = ErrorCode.INVALID_INPUT_VALUE.code
                )
            )
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(
        e: IllegalStateException
    ): ResponseEntity<ApiResponse<Unit>> {
        log.warn("IllegalStateException: {}", e.message)
        return ResponseEntity.badRequest()
            .body(
                ApiResponse.fail(
                    message = e.message ?: "Illegal State",
                    code = "BAD_REQUEST"
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        e: Exception
    ): ResponseEntity<ApiResponse<Unit>> {
        log.error("Unhandled Exception: ", e)
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.status)
            .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR))
    }
}
