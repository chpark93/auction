package com.ch.auction.admin.infrastructure.client.dto

import com.ch.auction.common.enums.UserRole
import com.ch.auction.common.enums.UserStatus
import java.time.LocalDateTime

object UserClientDtos {
    data class UserResponse(
        val userId: Long,
        val email: String,
        val nickname: String,
        val name: String,
        val phoneNumber: String,
        val role: UserRole,
        val status: UserStatus,
        val totalPoint: Long,
        val createdAt: LocalDateTime
    )
    
    data class UserListResponse(
        val content: List<UserResponse>,
        val totalElements: Long,
        val totalPages: Int,
        val number: Int,
        val size: Int
    )
    
    data class UpdateStatusRequest(
        val status: UserStatus,
        val reason: String?
    )
}

