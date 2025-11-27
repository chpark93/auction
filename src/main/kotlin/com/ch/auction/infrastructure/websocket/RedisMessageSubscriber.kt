package com.ch.auction.infrastructure.websocket

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class RedisMessageSubscriber(
    private val messagingTemplate: SimpMessagingTemplate,
    private val objectMapper: ObjectMapper
) : MessageListener {

    override fun onMessage(
        message: Message,
        pattern: ByteArray?
    ) {
        val body = String(message.body)
        try {
            val jsonNode: JsonNode = objectMapper.readTree(body)
            val auctionId = jsonNode.get("auctionId").asText()

            // 해당 actionId 구독 -> 메시지 전송
            messagingTemplate.convertAndSend("/topic/auctions/$auctionId", body)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

