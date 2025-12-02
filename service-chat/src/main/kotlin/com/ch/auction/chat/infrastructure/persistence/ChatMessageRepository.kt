package com.ch.auction.chat.infrastructure.persistence

import com.ch.auction.chat.domain.ChatMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

interface ChatMessageRepository : MongoRepository<ChatMessage, String> {
    fun findByRoomId(
        roomId: String,
        pageable: Pageable
    ): Page<ChatMessage>
}

