package com.ch.auction.auction.infrastructure.client.user

import com.ch.auction.auction.infrastructure.client.user.dtos.UserClientDtos
import com.ch.auction.common.ApiResponse
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.common.enums.UserStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UserClientFallback : UserClient {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getUserInfo(
        userId: Long
    ): ApiResponse<UserClientDtos.UserResponse> {
        logger.warn("UserClient fallback triggered for getUserInfo: userId=$userId")
        return ApiResponse.ok(
            data = UserClientDtos.UserResponse(
                id = userId,
                email = "unknown",
                nickname = "Unknown User",
                name = "Unknown",
                role = "USER",
                status = UserStatus.ACTIVE
            )
        )
    }

    override fun getUserPoint(
        userId: Long,
        auctionId: Long?
    ): ApiResponse<PointDTOs.PointResponse> {
        logger.warn("UserClient fallback triggered for getUserPoint: userId=$userId")
        return ApiResponse.ok(
            data = PointDTOs.PointResponse(
                userId = userId,
                totalPoint = 0L,
                lockedPoint = 0L,
                availablePoint = 0L
            )
        )
    }

    override fun holdPoint(
        userId: Long,
        request: UserClientDtos.HoldPointRequest
    ): ApiResponse<Unit> {
        logger.error("UserClient fallback triggered for holdPoint: userId=$userId - Operation not allowed")
        throw RuntimeException("User service is unavailable. Cannot hold point.")
    }

    override fun releasePoint(
        userId: Long,
        request: UserClientDtos.ReleasePointRequest
    ): ApiResponse<Unit> {
        logger.error("UserClient fallback triggered for releasePoint: userId=$userId - Operation not allowed")
        throw RuntimeException("User service is unavailable. Cannot release point.")
    }

    override fun usePoint(
        userId: Long,
        request: UserClientDtos.PointRequest
    ): ApiResponse<Unit> {
        logger.error("UserClient fallback triggered for usePoint: userId=$userId - Operation not allowed")
        throw RuntimeException("User service is unavailable. Cannot use point.")
    }

    override fun deductPoint(
        userId: Long,
        amount: Long,
        reason: String
    ): ApiResponse<Unit> {
        logger.error("UserClient fallback triggered for deductPoint: userId=$userId - Operation not allowed")
        throw RuntimeException("User service is unavailable. Cannot deduct point.")
    }

    override fun getUsersBatch(
        userIds: List<Long>
    ): ApiResponse<List<UserClientDtos.UserResponse>> {
        logger.warn("UserClient fallback triggered for getUsersBatch: userIds=$userIds")
        return ApiResponse.ok(
            data = userIds.map { userId ->
                UserClientDtos.UserResponse(
                    id = userId,
                    email = "unknown",
                    nickname = "Unknown User",
                    name = "Unknown",
                    role = "USER",
                    status = UserStatus.ACTIVE
                )
            }
        )
    }
}

