package com.ch.auction.chat.infrastructure.client.dto

object OrderClientDtos {
    data class OrderResponse(
        val id: Long,
        val buyerId: Long,
        val auctionId: Long
    )
}