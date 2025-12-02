package com.ch.auction.auction.interfaces.api

import com.ch.auction.auction.application.service.SellerAuctionService
import com.ch.auction.auction.interfaces.api.dto.admin.AuctionCreateRequest
import com.ch.auction.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/seller/auctions")
class SellerAuctionController(
    private val sellerAuctionService: SellerAuctionService
) {

    @PostMapping
    fun createAuction(
        @RequestHeader("X-User-Id") sellerId: Long,
        @RequestBody request: AuctionCreateRequest
    ): ResponseEntity<ApiResponse<Long>> {
        val auctionId = sellerAuctionService.createAuction(
            sellerId = sellerId,
            request = request
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = auctionId
            )
        )
    }

    @DeleteMapping("/{id}")
    fun deleteAuction(
        @RequestHeader("X-User-Id") sellerId: Long,
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        sellerAuctionService.deleteAuction(
            sellerId = sellerId,
            auctionId = id
        )

        return ResponseEntity.ok(
            ApiResponse.ok()
        )
    }
}