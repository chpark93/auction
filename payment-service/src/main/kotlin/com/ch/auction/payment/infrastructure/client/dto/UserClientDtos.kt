package com.ch.auction.payment.infrastructure.client.dto

import com.ch.auction.common.enums.UserRole
import com.ch.auction.common.enums.UserStatus

object UserClientDtos {

    data class UserPointResponse(
        val userId: Long,
        val totalPoint: Long,
        val lockedPoint: Long = 0L,
        val availablePoint: Long
    )

    data class UserResponse(
        val userId: Long,
        val email: String,
        val nickname: String,
        val role: UserRole,
        val totalPoint: Long,
        val status: UserStatus = UserStatus.ACTIVE
    )
}

