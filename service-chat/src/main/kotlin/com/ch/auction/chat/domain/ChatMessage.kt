package com.ch.auction.chat.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "chat_messages")
class ChatMessage(
    @Id
    val id: String? = null,

    @Indexed
    val roomId: String,

    val senderId: Long,

    val message: String,

    val timestamp: LocalDateTime = LocalDateTime.now(),

    var isRead: Boolean = false
)
