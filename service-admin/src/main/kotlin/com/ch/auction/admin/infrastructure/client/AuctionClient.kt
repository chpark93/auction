package com.ch.auction.admin.infrastructure.client

import com.ch.auction.admin.infrastructure.client.dto.AuctionClientDtos
import com.ch.auction.common.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*

@FeignClient(name = "service-auction")
interface AuctionClient {
    
    @GetMapping("/internal/auctions")
    fun getAuctions(
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) status: String?
    ): ApiResponse<AuctionClientDtos.AuctionListResponse>
    
    @GetMapping("/internal/auctions/{auctionId}")
    fun getAuction(
        @PathVariable auctionId: Long
    ): ApiResponse<AuctionClientDtos.AuctionResponse>
    
    @PostMapping("/internal/auctions/{auctionId}/approve")
    fun approveAuction(
        @PathVariable auctionId: Long
    ): ApiResponse<Unit>
    
    @PostMapping("/internal/auctions/{auctionId}/reject")
    fun rejectAuction(
        @PathVariable auctionId: Long,
        @RequestBody request: AuctionClientDtos.RejectRequest
    ): ApiResponse<Unit>
    
    @DeleteMapping("/internal/auctions/{auctionId}")
    fun deleteAuction(
        @PathVariable auctionId: Long
    ): ApiResponse<Unit>
    
    @PostMapping("/internal/auctions/{auctionId}/start")
    fun startAuction(
        @PathVariable auctionId: Long
    ): ApiResponse<Unit>
    
    @PostMapping("/internal/auctions/{auctionId}/end")
    fun endAuction(
        @PathVariable auctionId: Long
    ): ApiResponse<Unit>
}

