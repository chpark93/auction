package com.ch.auction.chat.interfaces.websocket

import com.ch.auction.common.ErrorCode
import com.ch.auction.domain.auth.port.AuthTokenParser
import com.ch.auction.domain.repository.TokenBlacklistRepository
import com.ch.auction.exception.BusinessException
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
class StompHandler(
    private val authTokenParser: AuthTokenParser,
    private val tokenBlacklistRepository: TokenBlacklistRepository
) : ChannelInterceptor {

    override fun preSend(
        message: Message<*>,
        channel: MessageChannel
    ): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)

        if (accessor.command == StompCommand.CONNECT) {
            val authHeader = accessor.getFirstNativeHeader("Authorization")
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.substring(7)

                if (tokenBlacklistRepository.exists(accessToken = token)) {
                    throw BusinessException(ErrorCode.TOKEN_INVALID)
                }

                try {
                    val claims = authTokenParser.parse(
                        token = token
                    )
                    val authorities = claims.roles.map { SimpleGrantedAuthority(it) }
                    val authentication = UsernamePasswordAuthenticationToken(claims.subject, null, authorities)

                    accessor.user = authentication
                } catch (_: Exception) {
                    throw BusinessException(ErrorCode.TOKEN_INVALID)
                }
            } else {
                throw BusinessException(ErrorCode.TOKEN_NOT_FOUND)
            }
        }

        return message
    }
}

