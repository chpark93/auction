package com.ch.auction.chat.application.service

import com.ch.auction.chat.domain.ChatMessage
import com.ch.auction.chat.infrastructure.persistence.ChatMessageRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val chatMessageRepository: ChatMessageRepository
) {
    fun saveMessage(
        message: ChatMessage
    ): ChatMessage {
        return chatMessageRepository.save(message)
    }

    fun getMessages(
        roomId: String,
        pageable: Pageable
    ): Page<ChatMessage> {
        return chatMessageRepository.findByRoomId(
            roomId = roomId,
            pageable = pageable
        )
    }
}

