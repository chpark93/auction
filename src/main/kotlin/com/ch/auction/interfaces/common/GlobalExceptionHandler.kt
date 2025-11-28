package com.ch.auction.interfaces.common

import com.ch.auction.application.exception.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        e: BusinessException
    ): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("BusinessException: {}", e.message)
        return ResponseEntity.status(e.errorCode.status)
            .body(ApiResponse.fail(e.errorCode))
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(
        e: NoResourceFoundException
    ): ResponseEntity<ApiResponse<Unit>> {
        // 정적 리소스 없음 예외는 로그 레벨을 낮추거나 생략하여 불필요한 에러 로그 방지
        logger.debug("NoResourceFoundException: {}", e.resourcePath)
        return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.status)
            .body(ApiResponse.fail(ErrorCode.RESOURCE_NOT_FOUND))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        e: IllegalArgumentException
    ): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("IllegalArgumentException: {}", e.message)
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
        logger.warn("IllegalStateException: {}", e.message)
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
        logger.error("Unhandled Exception: ", e)
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.status)
            .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR))
    }
}
