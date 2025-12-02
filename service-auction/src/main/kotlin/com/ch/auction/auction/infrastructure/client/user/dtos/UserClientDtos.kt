package com.ch.auction.auction.infrastructure.client.user.dtos

object UserClientDtos {

    data class UserResponse(
        val id: Long,
        val email: String,
        val nickname: String,
        val name: String,
        val role: String
    )
}