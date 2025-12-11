package com.ch.auction.auction.interfaces.internal

import com.ch.auction.auction.application.service.AuctionAdminService
import com.ch.auction.auction.application.service.AuctionService
import com.ch.auction.auction.domain.AuctionStatus
import com.ch.auction.auction.interfaces.api.dto.AuctionResponse
import com.ch.auction.auction.interfaces.api.dto.admin.AuctionAdminResponse
import com.ch.auction.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Internal Auction API", description = "내부 서비스 간 통신 - 경매 API")
@RestController
@RequestMapping("/internal/auctions")
class InternalAuctionController(
    private val auctionAdminService: AuctionAdminService,
    private val auctionService: AuctionService
) {

    @GetMapping
    fun getAuctions(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: AuctionStatus?
    ): ResponseEntity<ApiResponse<Page<AuctionAdminResponse>>> {
        val response = auctionAdminService.getAuctions(
            page = page,
            size = size,
            status = status
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }

    @GetMapping("/{id}")
    fun getAuction(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<AuctionAdminResponse>> {
        val response = auctionAdminService.getAuction(
            id = id
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }
    
    @Operation(summary = "경매 상세 조회 (service-chat 호출)", description = "채팅방 생성 시 경매 정보 조회")
    @GetMapping("/detail/{id}")
    fun getAuctionDetail(
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
    
    @PostMapping("/{id}/approve")
    fun approveAuction(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        auctionAdminService.approveAuction(
            id = id
        )

        return ResponseEntity.ok(
            ApiResponse.ok()
        )
    }

    @PostMapping("/{id}/reject")
    fun rejectAuction(
        @PathVariable id: Long,
        @RequestBody(required = false) request: Map<String, String>?
    ): ResponseEntity<ApiResponse<Unit>> {
        val reason = request?.get("reason") ?: "Rejected by admin"
        auctionAdminService.rejectAuction(
            id = id,
            reason = reason
        )

        return ResponseEntity.ok(
            ApiResponse.ok()
        )
    }
    
    @DeleteMapping("/{id}")
    fun deleteAuction(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        auctionAdminService.deleteAuction(
            id = id
        )

        return ResponseEntity.ok(
            ApiResponse.ok()
        )
    }

    @PostMapping("/{id}/start")
    fun startAuction(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        auctionAdminService.startAuction(
            id = id
        )

        return ResponseEntity.ok(
            ApiResponse.ok()
        )
    }

    @PostMapping("/{id}/end")
    fun endAuction(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        auctionAdminService.endAuction(
            id = id
        )

        return ResponseEntity.ok(
            ApiResponse.ok()
        )
    }
}

