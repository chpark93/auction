package com.ch.auction.auction.interfaces.api

import com.ch.auction.auction.application.service.AuctionAdminService
import com.ch.auction.auction.interfaces.api.dto.admin.AuctionAdminResponse
import com.ch.auction.auction.interfaces.api.dto.admin.AuctionCreateRequest
import com.ch.auction.auction.interfaces.api.dto.admin.AuctionUpdateRequest
import com.ch.auction.common.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Auction Admin Api", description = "관리자 경매 API")
@RestController
@RequestMapping("/api/v1/admin/auctions")
class AuctionAdminController(
    private val auctionAdminService: AuctionAdminService
) {

    @PostMapping
    fun createAuction(
        @RequestBody request: AuctionCreateRequest
    ): ResponseEntity<ApiResponse<AuctionAdminResponse>> {
        val response = auctionAdminService.createAuction(
            request = request
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }

    @PutMapping("/{id}")
    fun updateAuction(
        @PathVariable id: Long,
        @RequestBody request: AuctionUpdateRequest
    ): ResponseEntity<ApiResponse<AuctionAdminResponse>> {
        val response = auctionAdminService.updateAuction(
            id = id,
            request = request
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
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

    @PatchMapping("/{id}/approve")
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

    @PatchMapping("/{id}/reject")
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
}