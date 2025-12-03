package com.ch.auction.auction.infrastructure.client.user.dtos

import com.ch.auction.common.enums.UserStatus

object UserClientDtos {

    data class UserResponse(
        val id: Long,
        val email: String,
        val nickname: String,
        val name: String,
        val role: String,
        val status: UserStatus = UserStatus.ACTIVE
    )
}
