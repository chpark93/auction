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
    
    data class HoldPointRequest(
        val amount: Long,
        val reason: String,
        val auctionId: Long? = null
    )
    
    data class ReleasePointRequest(
        val amount: Long,
        val reason: String,
        val auctionId: Long? = null
    )
    
    data class PointRequest(
        val amount: Long
    )
}
