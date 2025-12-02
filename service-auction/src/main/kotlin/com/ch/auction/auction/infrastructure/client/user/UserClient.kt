package com.ch.auction.auction.infrastructure.client.user

import com.ch.auction.auction.infrastructure.client.user.dtos.UserClientDtos
import com.ch.auction.common.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "service-user", path = "/api/v1/users")
interface UserClient {

    @GetMapping("/{userId}")
    fun getUserInfo(
        @PathVariable("userId") userId: Long
    ): ApiResponse<UserClientDtos.UserResponse>
}