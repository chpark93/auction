package com.ch.auction.infrastructure.security

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.domain.auth.port.AuthTokenParser
import com.ch.auction.domain.repository.TokenBlacklistRepository
import com.ch.auction.interfaces.common.ErrorCode
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val authTokenParser: AuthTokenParser,
    private val tokenBlacklistRepository: TokenBlacklistRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        if (token != null) {
            try {
                if (tokenBlacklistRepository.exists(accessToken = token)) {
                    throw BusinessException(ErrorCode.TOKEN_INVALID)
                }

                val claims = authTokenParser.parse(token = token)
                val authorities = claims.roles.map { SimpleGrantedAuthority(it) }
                val authentication = UsernamePasswordAuthenticationToken(claims.subject, null, authorities)
                
                SecurityContextHolder.getContext().authentication = authentication

            } catch (e: BusinessException) {
                request.setAttribute("exception", e)
            } catch (_: Exception) {
                request.setAttribute("exception", BusinessException(ErrorCode.INTERNAL_SERVER_ERROR))
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(
        request: HttpServletRequest
    ): String? {
        val bearerToken = request.getHeader("Authorization")
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }

        return null
    }
}
