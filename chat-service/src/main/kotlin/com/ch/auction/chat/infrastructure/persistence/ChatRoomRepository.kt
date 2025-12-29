package com.ch.auction.chat.infrastructure.persistence

import com.ch.auction.chat.domain.ChatRoom
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface ChatRoomRepository : MongoRepository<ChatRoom, String> {
    fun findByAuctionId(
        auctionId: Long
    ): Optional<ChatRoom>
}

