package com.ch.auction.user.interfaces.api.dto

import com.ch.auction.common.enums.UserRole
import com.ch.auction.common.enums.UserStatus
import com.ch.auction.user.domain.User
import java.time.LocalDateTime

data class UserResponse(
    val userId: Long,
    val email: String,
    val nickname: String,
    val name: String?,
    val phoneNumber: String?,
    val role: UserRole,
    val status: UserStatus,
    val totalPoint: Long,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(
            user: User
        ): UserResponse {
            return UserResponse(
                userId = user.id!!,
                email = user.email,
                nickname = user.nickname,
                name = user.name,
                phoneNumber = user.phoneNumber,
                role = user.role,
                status = user.status,
                totalPoint = user.point,
                createdAt = user.createdAt
            )
        }
    }
}

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

