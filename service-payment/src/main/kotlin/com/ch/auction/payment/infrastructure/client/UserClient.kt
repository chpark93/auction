package com.ch.auction.payment.infrastructure.client

import com.ch.auction.common.ApiResponse
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.payment.infrastructure.client.dto.UserClientDtos
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*

@FeignClient(name = "service-user")
interface UserClient {

    @GetMapping("/api/v1/users/{userId}")
    fun getUser(
        @PathVariable("userId") userId: Long
    ): ApiResponse<UserClientDtos.UserPointResponse>

    @GetMapping("/api/v1/users/email")
    fun getUserByEmail(
        @RequestParam("email") email: String
    ): ApiResponse<UserClientDtos.UserPointResponse>

    @PostMapping("/api/v1/users/{userId}/points/charge")
    fun chargePoint(
        @PathVariable("userId") userId: Long,
        @RequestBody request: PointDTOs.PointRequest
    ): ApiResponse<UserClientDtos.UserPointResponse>

    @PostMapping("/api/v1/users/{userId}/points/use")
    fun usePoint(
        @PathVariable("userId") userId: Long,
        @RequestBody request: PointDTOs.PointRequest
    ): ApiResponse<UserClientDtos.UserPointResponse>
}

