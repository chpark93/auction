package com.ch.auction.admin.infrastructure.client

import com.ch.auction.admin.infrastructure.client.dto.UserClientDtos
import com.ch.auction.common.ApiResponse
import com.ch.auction.common.enums.UserStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UserClientFallback : UserClient {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getUsers(
        page: Int?,
        size: Int?,
        status: UserStatus?
    ): ApiResponse<UserClientDtos.UserListResponse> {
        logger.error("UserClient fallback triggered for getUsers")
        throw RuntimeException("User service is unavailable")
    }

    override fun getUser(
        userId: Long
    ): ApiResponse<UserClientDtos.UserResponse> {
        logger.warn("UserClient fallback triggered for getUser: userId=$userId")
        throw RuntimeException("User service is unavailable")
    }

    override fun getUsersBatch(
        ids: List<Long>
    ): ApiResponse<Map<Long, UserClientDtos.UserResponse>> {
        logger.warn("UserClient fallback triggered for getUsersBatch")
        return ApiResponse.ok(data = emptyMap())
    }

    override fun updateUserStatus(
        userId: Long,
        request: UserClientDtos.UpdateStatusRequest
    ): ApiResponse<Unit> {
        logger.error("UserClient fallback triggered for updateUserStatus: userId=$userId")
        throw RuntimeException("User service is unavailable. Cannot update user status.")
    }
}

