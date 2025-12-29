package com.ch.auction.chat.interfaces.config

import com.ch.auction.chat.interfaces.websocket.StompHandler
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val stompHandler: StompHandler
) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(
        config: MessageBrokerRegistry
    ) {
        // topic message -> 메시지 브로커(Redis)로 라우팅
        config.enableSimpleBroker("/topic")
        // app message -> @MessageMapping이 붙은 메서드로 라우팅
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(
        registry: StompEndpointRegistry
    ) {
        registry.addEndpoint("/ws-auction")
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }

    override fun configureClientInboundChannel(
        registration: ChannelRegistration
    ) {
        registration.interceptors(stompHandler)
    }
}
