package com.ch.auction.interfaces.api

import com.ch.auction.application.service.AuctionAdminService
import com.ch.auction.interfaces.api.dto.admin.AuctionAdminResponse
import com.ch.auction.interfaces.api.dto.admin.AuctionCreateRequest
import com.ch.auction.interfaces.api.dto.admin.AuctionUpdateRequest
import com.ch.auction.interfaces.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

        return ResponseEntity.ok(ApiResponse.ok(
            data = response
        ))
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

        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @DeleteMapping("/{id}")
    fun deleteAuction(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        auctionAdminService.deleteAuction(
            id = id
        )

        return ResponseEntity.ok(ApiResponse.ok())
    }

    @PostMapping("/{id}/start")
    fun startAuction(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        auctionAdminService.startAuction(id)
        return ResponseEntity.ok(ApiResponse.ok())
    }

    @PostMapping("/{id}/end")
    fun endAuction(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        auctionAdminService.endAuction(id)
        return ResponseEntity.ok(ApiResponse.ok())
    }
}

