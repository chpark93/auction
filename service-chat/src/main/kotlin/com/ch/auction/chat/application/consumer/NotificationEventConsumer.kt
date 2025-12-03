package com.ch.auction.chat.application.consumer

import com.ch.auction.common.event.NotificationEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class NotificationEventConsumer(
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["notification-send"],
        groupId = "chat-notification-group"
    )
    fun handleNotification(
        event: NotificationEvent
    ) {
        logger.info("Received NotificationEvent: {}", event)

        messagingTemplate.convertAndSendToUser(
            event.userId.toString(),
            "/queue/notifications",
            event
        )
    }
}

