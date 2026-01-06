package com.ch.auction.common.security

import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

class HeaderAuthenticationFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val userId = request.getHeader("X-User-Id")
            val email = request.getHeader("X-User-Email")
            val roles = request.getHeader("X-User-Roles")

            if (userId != null && email != null) {
                val authorities = if (StringUtils.hasText(roles)) {
                    roles.split(",").map { SimpleGrantedAuthority(it.trim()) }
                } else {
                    emptyList()
                }
                
                val authentication = UsernamePasswordAuthenticationToken(
                    email,
                    userId,
                    authorities
                )

                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (_: Exception) {
            request.setAttribute("exception", BusinessException(ErrorCode.INTERNAL_SERVER_ERROR))
        }

        filterChain.doFilter(request, response)
    }
}

