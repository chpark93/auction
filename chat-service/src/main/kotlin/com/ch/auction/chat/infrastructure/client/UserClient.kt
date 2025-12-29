package com.ch.auction.chat.infrastructure.client

import com.ch.auction.chat.infrastructure.client.dto.UserClientDtos
import com.ch.auction.common.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "service-user")
interface UserClient {

    @GetMapping("/internal/users/email")
    fun getUserByEmail(
        @RequestParam("email") email: String
    ): ApiResponse<UserClientDtos.UserResponse>
}

