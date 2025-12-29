package com.ch.auction.user.interfaces.api

import com.ch.auction.common.ApiResponse
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.user.application.service.PointTransactionService
import com.ch.auction.user.application.service.UserService
import com.ch.auction.user.interfaces.api.dto.PointTransactionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Point API", description = "포인트 관리 API")
@RestController
@RequestMapping("/api/v1/users")
class PointController(
    private val userService: UserService,
    private val pointTransactionService: PointTransactionService
) {

    @Operation(summary = "포인트 충전", description = "사용자 포인트 충전")
    @PostMapping("/{userId}/points/charge")
    fun chargePoint(
        @PathVariable userId: Long,
        @RequestBody request: PointDTOs.PointRequest
    ): ResponseEntity<ApiResponse<PointDTOs.PointResponse>> {
        val response = userService.chargePoint(
            userId = userId,
            amount = request.amount
        )
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }
    
    @Operation(summary = "포인트 사용", description = "사용자 포인트 사용")
    @PostMapping("/{userId}/points/use")
    fun usePoint(
        @PathVariable userId: Long,
        @RequestBody request: PointDTOs.PointRequest
    ): ResponseEntity<ApiResponse<PointDTOs.PointResponse>> {
        val response = userService.usePoint(
            userId = userId,
            amount = request.amount
        )
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }
    
    @Operation(summary = "포인트 조회", description = "사용자 포인트 정보 조회")
    @GetMapping("/{userId}/points")
    fun getUserPoint(
        @PathVariable userId: Long
    ): ResponseEntity<ApiResponse<PointDTOs.PointResponse>> {
        val response = userService.getUserPoint(
            userId = userId
        )
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }
    
    @Operation(summary = "포인트 사용 내역 조회", description = "사용자 포인트 거래 내역 조회")
    @GetMapping("/{userId}/points/history")
    fun getPointHistory(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<PointTransactionResponse>>> {
        val history = pointTransactionService.getUserPointHistory(
            userId = userId,
            page = page,
            size = size
        )
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = history.map {
                    PointTransactionResponse.from(
                        transaction = it
                    )
                }
            )
        )
    }
}

