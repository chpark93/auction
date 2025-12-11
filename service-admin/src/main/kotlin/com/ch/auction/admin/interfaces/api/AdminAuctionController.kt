package com.ch.auction.admin.interfaces.api

import com.ch.auction.admin.application.service.AdminAuctionService
import com.ch.auction.admin.infrastructure.client.dto.AuctionClientDtos
import com.ch.auction.admin.interfaces.api.dto.AdminAuctionListResponse
import com.ch.auction.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Admin Auction API", description = "관리자 경매 관리 API")
@RestController
@RequestMapping("/api/v1/admin/auctions")
@PreAuthorize("hasRole('ADMIN')")
class AdminAuctionController(
    private val adminAuctionService: AdminAuctionService
) {

    @Operation(summary = "경매 목록 조회", description = "전체 경매 목록을 조회")
    @GetMapping
    fun getAuctions(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<ApiResponse<AdminAuctionListResponse>> {
        val response = adminAuctionService.getAuctionsWithSellerInfo(
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
    
    @Operation(summary = "승인 대기 경매 조회", description = "승인 대기 중인 경매 목록을 조회")
    @GetMapping("/pending")
    fun getPendingAuctions(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<AuctionClientDtos.AuctionListResponse>> {
        val response = adminAuctionService.getPendingAuctions(
            page = page,
            size = size
        )
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }
    
    @Operation(summary = "경매 상세 조회", description = "특정 경매의 상세 정보를 조회")
    @GetMapping("/{auctionId}")
    fun getAuction(
        @PathVariable auctionId: Long
    ): ResponseEntity<ApiResponse<AuctionClientDtos.AuctionResponse>> {
        val response = adminAuctionService.getAuction(auctionId)
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }
    
    @Operation(summary = "경매 승인", description = "승인 대기 중인 경매를 승인")
    @PostMapping("/{auctionId}/approve")
    fun approveAuction(
        @PathVariable auctionId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        adminAuctionService.approveAuction(auctionId)
        
        return ResponseEntity.ok(ApiResponse.ok())
    }
    
    @Operation(summary = "경매 거부", description = "승인 대기 중인 경매를 거절")
    @PostMapping("/{auctionId}/reject")
    fun rejectAuction(
        @PathVariable auctionId: Long,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<ApiResponse<Unit>> {
        val reason = request["reason"] ?: "관리자에 의한 거부"
        
        adminAuctionService.rejectAuction(
            auctionId = auctionId,
            reason = reason
        )
        
        return ResponseEntity.ok(ApiResponse.ok())
    }
    
    @Operation(summary = "경매 삭제", description = "경매를 삭제")
    @DeleteMapping("/{auctionId}")
    fun deleteAuction(
        @PathVariable auctionId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        adminAuctionService.deleteAuction(auctionId)
        
        return ResponseEntity.ok(ApiResponse.ok())
    }
    
    @Operation(summary = "경매 강제 시작", description = "경매 강제 시작")
    @PostMapping("/{auctionId}/start")
    fun startAuction(
        @PathVariable auctionId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        adminAuctionService.startAuction(auctionId)
        
        return ResponseEntity.ok(ApiResponse.ok())
    }
    
    @Operation(summary = "경매 강제 종료", description = "경매 강제 종료")
    @PostMapping("/{auctionId}/end")
    fun endAuction(
        @PathVariable auctionId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        adminAuctionService.endAuction(auctionId)
        
        return ResponseEntity.ok(ApiResponse.ok())
    }
}

