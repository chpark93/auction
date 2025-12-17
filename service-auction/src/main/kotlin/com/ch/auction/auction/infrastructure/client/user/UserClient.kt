package com.ch.auction.auction.infrastructure.client.user

import com.ch.auction.auction.infrastructure.client.user.dtos.UserClientDtos
import com.ch.auction.common.ApiResponse
import com.ch.auction.common.dto.PointDTOs
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "service-user",
    path = "/internal/users",
    fallback = UserClientFallback::class
)
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
        @PathVariable("userId") userId: Long,
        @RequestParam(required = false) auctionId: Long?
    ): ApiResponse<PointDTOs.PointResponse>
    
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
    
    @PostMapping("/{userId}/points/use")
    fun usePoint(
        @PathVariable("userId") userId: Long,
        @RequestBody request: UserClientDtos.PointRequest
    ): ApiResponse<Unit>
    
    @PostMapping("/{userId}/points/deduct")
    fun deductPoint(
        @PathVariable("userId") userId: Long,
        @RequestParam amount: Long,
        @RequestParam reason: String
    ): ApiResponse<Unit>
}