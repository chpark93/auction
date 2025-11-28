package com.ch.auction.interfaces.api

import com.ch.auction.application.service.SellerAuctionService
import com.ch.auction.interfaces.api.dto.admin.AuctionCreateRequest
import com.ch.auction.interfaces.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/seller/auctions")
class SellerAuctionController(
    private val sellerAuctionService: SellerAuctionService
) {

    @PostMapping
    fun createAuction(
        // TODO: jwt 토큰 방식으로 변경 예정
        @RequestHeader("X-User-Id") sellerId: Long,
        @RequestBody request: AuctionCreateRequest
    ): ResponseEntity<ApiResponse<Long>> {
        val auctionId = sellerAuctionService.createAuction(sellerId, request)
        return ResponseEntity.ok(ApiResponse.ok(auctionId))
    }
    
    @DeleteMapping("/{id}")
    fun deleteAuction(
        // TODO: jwt 토큰 방식으로 변경 예정
        @RequestHeader("X-User-Id") sellerId: Long,
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        sellerAuctionService.deleteAuction(sellerId, id)
        return ResponseEntity.ok(ApiResponse.ok())
    }
}

