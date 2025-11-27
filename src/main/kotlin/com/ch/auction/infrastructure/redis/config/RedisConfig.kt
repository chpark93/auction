package com.ch.auction.infrastructure.redis.config

import com.ch.auction.infrastructure.redis.RedisMessageSubscriber
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter

@Configuration
class RedisConfig {

    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
        messageListenerAdapter: MessageListenerAdapter
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        container.addMessageListener(
            messageListenerAdapter,
            ChannelTopic("auction-updates")
        )
        return container
    }

    @Bean
    fun messageListenerAdapter(
        subscriber: RedisMessageSubscriber
    ): MessageListenerAdapter {
        return MessageListenerAdapter(subscriber)
    }
}

