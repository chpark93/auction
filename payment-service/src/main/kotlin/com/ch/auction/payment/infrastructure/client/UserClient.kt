package com.ch.auction.payment.infrastructure.client

import com.ch.auction.common.ApiResponse
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.payment.infrastructure.client.dto.UserClientDtos
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*

@FeignClient(name = "service-user")
interface UserClient {

    @GetMapping("/internal/users/{userId}")
    fun getUser(
        @PathVariable("userId") userId: Long
    ): ApiResponse<UserClientDtos.UserPointResponse>

    @GetMapping("/internal/users/email/points")
    fun getUserByEmail(
        @RequestParam("email") email: String
    ): ApiResponse<UserClientDtos.UserPointResponse>

    @PostMapping("/internal/users/{userId}/points/charge")
    fun chargePoint(
        @PathVariable("userId") userId: Long,
        @RequestBody request: PointDTOs.PointRequest
    ): ApiResponse<UserClientDtos.UserPointResponse>

    @PostMapping("/internal/users/{userId}/points/use")
    fun usePoint(
        @PathVariable("userId") userId: Long,
        @RequestBody request: PointDTOs.PointRequest
    ): ApiResponse<UserClientDtos.UserPointResponse>
}

