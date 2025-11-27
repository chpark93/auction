package com.ch.auction.interfaces.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(
        config: MessageBrokerRegistry
    ) {
        // /topic으로 시작하는 메시지 -> 메시지 브로커(Redis)로 라우팅
        config.enableSimpleBroker("/topic")
        // /app으로 시작하는 메시지 -> @MessageMapping이 붙은 메서드로 라우팅
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(
        registry: StompEndpointRegistry
    ) {
        // WebSocket Endpoint 등록
        registry.addEndpoint("/ws-auction")
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }
}

