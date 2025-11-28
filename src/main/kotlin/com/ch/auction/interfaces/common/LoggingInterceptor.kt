package com.ch.auction.interfaces.common

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.lang.Exception

@Component
class LoggingInterceptor : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        logger.info("[REQUEST] {} {}", request.method, request.requestURI)
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        if (ex != null) {
            logger.error("[RESPONSE] {} {} {} - ERROR: {}", request.method, request.requestURI, response.status, ex.message)
        } else {
            logger.info("[RESPONSE] {} {} {}", request.method, request.requestURI, response.status)
        }
    }
}

