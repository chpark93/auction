package com.ch.auction.auction.interfaces.api

import com.ch.auction.auction.application.service.AuctionService
import com.ch.auction.auction.interfaces.api.dto.AuctionResponse
import com.ch.auction.auction.interfaces.api.dto.BidRequest
import com.ch.auction.auction.interfaces.api.dto.BidResponse
import com.ch.auction.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auctions")
class AuctionController(
    private val auctionService: AuctionService
) {

    @PostMapping("/{id}/bid")
    fun placeBid(
        @PathVariable id: Long,
        @RequestBody request: BidRequest
    ): ResponseEntity<ApiResponse<BidResponse>> {
        val newPrice = auctionService.placeBid(
            auctionId = id,
            userId = request.userId,
            amount = request.amount
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = BidResponse(
                    newPrice = newPrice
                )
            )
        )
    }

    @GetMapping("/{id}/price")
    fun getCurrentPrice(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Long>> {
        val currentPrice = auctionService.getAuctionCurrentPrice(
            auctionId = id
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = currentPrice
            )
        )
    }

    @GetMapping("/{id}")
    fun getAuction(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<AuctionResponse>> {
        val response = auctionService.getAuction(
            auctionId = id
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }
}
