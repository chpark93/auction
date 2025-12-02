package com.ch.auction.chat.infrastructure.client

import com.ch.auction.chat.infrastructure.client.dto.AuctionDtos
import com.ch.auction.common.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "service-auction")
interface AuctionClient {

    @GetMapping("/api/v1/auctions/{auctionId}")
    fun getAuction(
        @PathVariable("auctionId") auctionId: Long
    ): ApiResponse<AuctionDtos.AuctionResponse>
}

