package com.ch.auction.chat.infrastructure.client

import com.ch.auction.chat.infrastructure.client.dto.AuctionDtos
import com.ch.auction.common.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "auction-service", fallback = AuctionClientFallback::class)
interface AuctionClient {

    @GetMapping("/internal/auctions/detail/{auctionId}")
    fun getAuction(
        @PathVariable("auctionId") auctionId: Long
    ): ApiResponse<AuctionDtos.AuctionResponse>
}

@Component
class AuctionClientFallback : AuctionClient {
    override fun getAuction(
        auctionId: Long
    ): ApiResponse<AuctionDtos.AuctionResponse> {
        throw RuntimeException("Auction service is currently unavailable")
    }
}
