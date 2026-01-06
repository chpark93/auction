package com.ch.auction.common.filter

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.*

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnWebApplication(type = Type.SERVLET)
class LogFilter : Filter {

    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain
    ) {
        val traceId = UUID.randomUUID().toString().substring(0, 8)
        MDC.put("traceId", traceId)

        if (response is HttpServletResponse) {
            response.addHeader("X-Trace-Id", traceId)
        }

        try {
            chain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}

