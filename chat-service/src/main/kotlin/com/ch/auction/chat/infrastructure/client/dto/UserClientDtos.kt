package com.ch.auction.chat.infrastructure.client.dto

object UserClientDtos {
    data class UserResponse(
        val userId: Long,
        val email: String,
        val nickname: String
    )
}