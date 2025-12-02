package com.ch.auction.chat.infrastructure.client.dto

object AuctionDtos {
    data class AuctionResponse(
        val id: Long,
        val sellerId: Long,
        val status: String
    )
}