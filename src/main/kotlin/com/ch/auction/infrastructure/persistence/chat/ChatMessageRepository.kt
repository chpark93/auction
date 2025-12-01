package com.ch.auction.infrastructure.persistence.chat

import com.ch.auction.domain.chat.ChatMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

interface ChatMessageRepository : MongoRepository<ChatMessage, String> {
    fun findByRoomId(
        roomId: String,
        pageable: Pageable
    ): Page<ChatMessage>
}

