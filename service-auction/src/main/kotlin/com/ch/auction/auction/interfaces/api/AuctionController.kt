package com.ch.auction.auction.interfaces.api

import com.ch.auction.auction.application.service.AuctionService
import com.ch.auction.auction.interfaces.api.dto.AuctionListResponse
import com.ch.auction.auction.interfaces.api.dto.AuctionResponse
import com.ch.auction.auction.interfaces.api.dto.BidRequest
import com.ch.auction.auction.interfaces.api.dto.BidResponse
import com.ch.auction.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Auction API", description = "경매 API")
@RestController
@RequestMapping("/api/v1/auctions")
class AuctionController(
    private val auctionService: AuctionService
) {

    @Operation(summary = "경매 목록 조회", description = "진행 중, 준비 중, 승인된 경매 목록을 조회합니다")
    @GetMapping
    fun getAuctions(
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<AuctionListResponse>>> {
        val response = auctionService.getAuctions(
            pageable = pageable
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }

    @Operation(summary = "경매 상세 조회", description = "경매 ID로 상세 정보를 조회합니다")
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

    @Operation(summary = "입찰하기", description = "경매에 입찰합니다")
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

    @Operation(summary = "현재가 조회", description = "경매의 현재가를 조회합니다")
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
}
