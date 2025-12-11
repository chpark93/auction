package com.ch.auction.admin.interfaces.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class HeaderAuthenticationFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val userEmail = request.getHeader("X-User-Email")
        val userId = request.getHeader("X-User-Id")
        val userRoles = request.getHeader("X-User-Roles")

        if (userEmail != null && userId != null && userRoles != null) {
            val authorities = userRoles.split(",")
                .map { SimpleGrantedAuthority(it.trim()) }
                .toList()

            val authentication = UsernamePasswordAuthenticationToken(
                userEmail,
                null,
                authorities
            )

            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }
}

