package com.ch.auction.domain.chat

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "chat_rooms")
class ChatRoom(
    @Id
    val id: String? = null,

    @Indexed(unique = true)
    val auctionId: Long,

    val sellerId: Long,

    val buyerId: Long,

    val createdAt: LocalDateTime = LocalDateTime.now()
)

