package com.ch.auction.common.security

import com.ch.auction.common.ApiResponse
import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val exception = request.getAttribute("exception") as? BusinessException
        
        val errorCode = exception?.errorCode ?: ErrorCode.UNAUTHORIZED

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val responseBody = ApiResponse.fail(
            errorCode = errorCode
        )

        objectMapper.writeValue(response.outputStream, responseBody)
    }
}

