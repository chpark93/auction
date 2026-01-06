package com.ch.auction.chat.infrastructure.client

import com.ch.auction.chat.infrastructure.client.dto.OrderClientDtos
import com.ch.auction.common.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "payment-service")
interface OrderClient {

    @GetMapping("/internal/payments/orders")
    fun getOrderByAuctionId(
        @RequestParam("auctionId") auctionId: Long
    ): ApiResponse<OrderClientDtos.OrderResponse>
}

