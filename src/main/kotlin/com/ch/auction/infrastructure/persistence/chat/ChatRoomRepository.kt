package com.ch.auction.infrastructure.persistence.chat

import com.ch.auction.domain.chat.ChatRoom
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface ChatRoomRepository : MongoRepository<ChatRoom, String> {
    fun findByAuctionId(
        auctionId: Long
    ): Optional<ChatRoom>
}

