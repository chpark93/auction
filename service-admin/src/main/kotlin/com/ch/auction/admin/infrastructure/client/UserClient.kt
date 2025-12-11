package com.ch.auction.admin.infrastructure.client

import com.ch.auction.admin.infrastructure.client.dto.UserClientDtos
import com.ch.auction.common.ApiResponse
import com.ch.auction.common.enums.UserStatus
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*

@FeignClient(name = "service-user")
interface UserClient {
    
    @GetMapping("/internal/users")
    fun getUsers(
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) status: UserStatus?
    ): ApiResponse<UserClientDtos.UserListResponse>
    
    @GetMapping("/internal/users/{userId}")
    fun getUser(
        @PathVariable userId: Long
    ): ApiResponse<UserClientDtos.UserResponse>

    @GetMapping("/internal/users/batch")
    fun getUsersBatch(
        @RequestParam ids: List<Long>
    ): ApiResponse<Map<Long, UserClientDtos.UserResponse>>
    
    @PatchMapping("/internal/users/{userId}/status")
    fun updateUserStatus(
        @PathVariable userId: Long,
        @RequestBody request: UserClientDtos.UpdateStatusRequest
    ): ApiResponse<Unit>
}

