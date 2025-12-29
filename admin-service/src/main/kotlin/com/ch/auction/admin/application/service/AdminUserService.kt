package com.ch.auction.admin.application.service

import com.ch.auction.admin.infrastructure.client.UserClient
import com.ch.auction.admin.infrastructure.client.dto.UserClientDtos
import com.ch.auction.common.ErrorCode
import com.ch.auction.common.enums.UserStatus
import com.ch.auction.exception.BusinessException
import org.springframework.stereotype.Service

@Service
class AdminUserService(
    private val userClient: UserClient
) {
    /**
     * 회원 목록 조회
     */
    fun getUsers(
        page: Int = 0,
        size: Int = 20,
        status: UserStatus?
    ): UserClientDtos.UserListResponse {
        val response = userClient.getUsers(
            page = page,
            size = size,
            status = status
        )
        
        return response.data ?: UserClientDtos.UserListResponse(
            content = emptyList(),
            totalElements = 0,
            totalPages = 0,
            number = page,
            size = size
        )
    }
    
    /**
     * 회원 상세 조회
     */
    fun getUser(
        userId: Long
    ): UserClientDtos.UserResponse {
        val response = userClient.getUser(
            userId = userId
        )

        return response.data ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }
    
    /**
     * 회원 상태 변경 (차단/해제)
     */
    fun updateUserStatus(
        userId: Long,
        status: UserStatus,
        reason: String?
    ) {
        userClient.updateUserStatus(
            userId = userId,
            request = UserClientDtos.UpdateStatusRequest(
                status = status,
                reason = reason
            )
        )
    }
    
    /**
     * 회원 차단
     */
    fun banUser(
        userId: Long,
        reason: String
    ) {
        updateUserStatus(
            userId = userId,
            status = UserStatus.BANNED,
            reason = reason
        )
    }
    
    /**
     * 회원 차단 해제
     */
    fun unbanUser(
        userId: Long
    ) {
        updateUserStatus(
            userId = userId,
            status = UserStatus.ACTIVE,
            reason = "관리자에 의한 차단 해제"
        )
    }
}

