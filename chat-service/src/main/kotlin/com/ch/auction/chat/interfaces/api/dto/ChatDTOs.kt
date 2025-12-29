package com.ch.auction.chat.interfaces.api.dto

object ChatDTOs {
    data class ChatMessageRequest(
        val roomId: String,
        val senderId: Long,
        val message: String
    )

    data class ChatRoomResponse(
        val roomId: String,
        val auctionId: Long,
        val sellerId: Long,
        val buyerId: Long
    )
}

