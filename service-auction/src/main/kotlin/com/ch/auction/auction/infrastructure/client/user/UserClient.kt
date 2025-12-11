package com.ch.auction.auction.infrastructure.client.user

import com.ch.auction.auction.infrastructure.client.user.dtos.UserClientDtos
import com.ch.auction.common.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "service-user", path = "/internal/users")
interface UserClient {

    @GetMapping("/{userId}")
    fun getUserInfo(
        @PathVariable("userId") userId: Long
    ): ApiResponse<UserClientDtos.UserResponse>

    @GetMapping("/batch")
    fun getUsersBatch(
        @RequestParam("userIds") userIds: List<Long>
    ): ApiResponse<List<UserClientDtos.UserResponse>>
    
    @GetMapping("/{userId}/points")
    fun getUserPoint(
        @PathVariable("userId") userId: Long
    ): ApiResponse<Long>
    
    @PostMapping("/{userId}/points/hold")
    fun holdPoint(
        @PathVariable("userId") userId: Long,
        @RequestBody request: UserClientDtos.HoldPointRequest
    ): ApiResponse<Unit>
    
    @PostMapping("/{userId}/points/release")
    fun releasePoint(
        @PathVariable("userId") userId: Long,
        @RequestBody request: UserClientDtos.ReleasePointRequest
    ): ApiResponse<Unit>
}