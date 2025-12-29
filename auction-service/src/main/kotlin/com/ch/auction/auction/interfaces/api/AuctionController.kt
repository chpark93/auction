package com.ch.auction.auction.interfaces.api

import com.ch.auction.auction.application.service.AuctionService
import com.ch.auction.auction.interfaces.api.dto.*
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
    ): ResponseEntity<ApiResponse<AuctionDetailResponse>> {
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
    ): ResponseEntity<ApiResponse<CurrentPriceResponse>> {
        val response = auctionService.getAuctionCurrentPrice(
            auctionId = id
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }

    @Operation(summary = "입찰 내역 조회", description = "경매 입찰 내역 조회")
    @GetMapping("/{id}/bids")
    fun getBidHistory(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<ApiResponse<List<BidHistoryResponse>>> {
        val bidHistory = auctionService.getBidHistory(
            auctionId = id,
            limit = limit
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = bidHistory
            )
        )
    }
    
    @Operation(summary = "입찰 포기", description = "사용자 입찰 포기")
    @PostMapping("/{id}/cancel-bid")
    fun cancelBid(
        @PathVariable id: Long,
        @RequestBody request: CancelBidRequest,
        @RequestParam userId: Long
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val result = auctionService.cancelBid(
            auctionId = id,
            userId = userId,
            reason = request.reason
        )
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = result
            )
        )
    }
    
    @Operation(summary = "사용자 입찰 중 경매 목록", description = "사용자가 입찰 중인 경매 목록 조회")
    @GetMapping("/users/{userId}/bidding")
    fun getMyBiddingAuctions(
        @PathVariable userId: Long
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val result = auctionService.getMyBiddingAuctions(userId)
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = result
            )
        )
    }
}
