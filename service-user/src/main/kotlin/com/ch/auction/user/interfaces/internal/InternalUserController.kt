package com.ch.auction.user.interfaces.internal

import com.ch.auction.common.ApiResponse
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.common.enums.UserStatus
import com.ch.auction.user.application.service.UserService
import com.ch.auction.user.interfaces.api.dto.UpdateStatusRequest
import com.ch.auction.user.interfaces.api.dto.UserListResponse
import com.ch.auction.user.interfaces.api.dto.UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Internal User API", description = "내부 서비스 간 통신 - 회원 API")
@RestController
@RequestMapping("/internal/users")
class InternalUserController(
    private val userService: UserService
) {

    @Operation(summary = "회원 목록 조회", description = "회원 목록 조회")
    @GetMapping
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: UserStatus?
    ): ResponseEntity<ApiResponse<UserListResponse>> {
        val response = userService.getUsers(
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
    
    @Operation(summary = "회원 상세 조회", description = "특정 회원 상세 정보 조회")
    @GetMapping("/{userId}")
    fun getUser(
        @PathVariable userId: Long
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userService.getUser(
            userId = userId
        )
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = user
            )
        )
    }
    
    @Operation(summary = "회원 일괄 조회 (Batch)", description = "회원 정보 일괄 조회")
    @GetMapping("/batch")
    fun getUsersBatch(
        @RequestParam userIds: List<Long>
    ): ResponseEntity<ApiResponse<List<UserResponse>>> {
        val users = userService.getUsersBatch(
            userIds = userIds
        )
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = users.values.toList()
            )
        )
    }
    
    @Operation(summary = "회원 상태 변경", description = "회원 상태 변경 (차단/해제)")
    @PatchMapping("/{userId}/status")
    fun updateUserStatus(
        @PathVariable userId: Long,
        @RequestBody request: UpdateStatusRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        userService.updateUserStatus(
            userId = userId,
            status = request.status,
            reason = request.reason
        )
        
        return ResponseEntity.ok(ApiResponse.ok())
    }
    
    @Operation(summary = "회원 조회[이메일]", description = "회원 정보 조회 (service-chat 호출)")
    @GetMapping("/email")
    fun getUserByEmail(
        @RequestParam email: String
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val response = userService.getUserByEmailAsUserResponse(email)
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }
    
    @Operation(summary = "포인트 정보 조회[이메일]", description = "회원 포인트 정보 조회 (service-payment 호출)")
    @GetMapping("/email/points")
    fun getUserPointsByEmail(
        @RequestParam email: String
    ): ResponseEntity<ApiResponse<PointDTOs.PointResponse>> {
        val response = userService.getUserByEmail(email)
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }
    
    @Operation(summary = "포인트 충전", description = "회원 포인트 충전 (service-payment 호출)")
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
    
    @Operation(summary = "포인트 사용", description = "회원 포인트 사용 (service-payment 호출)")
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
    
    @Operation(summary = "포인트 정보 조회", description = "회원 포인트 정보 조회 (재입찰 시 auctionId 전달)")
    @GetMapping("/{userId}/points")
    fun getUserPoint(
        @PathVariable userId: Long,
        @RequestParam(required = false) auctionId: Long?
    ): ResponseEntity<ApiResponse<PointDTOs.PointResponse>> {
        val response = userService.getUserPoint(
            userId = userId,
            auctionId = auctionId
        )
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }
    
    @Operation(summary = "포인트 홀드", description = "입찰 시 포인트 홀드")
    @PostMapping("/{userId}/points/hold")
    fun holdPoint(
        @PathVariable userId: Long,
        @RequestBody request: PointDTOs.HoldPointRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        userService.holdPoint(
            userId = userId,
            amount = request.amount,
            reason = request.reason,
            auctionId = request.auctionId
        )
        
        return ResponseEntity.ok(ApiResponse.ok())
    }
    
    @Operation(summary = "포인트 해제", description = "입찰 실패 시 포인트 해제")
    @PostMapping("/{userId}/points/release")
    fun releasePoint(
        @PathVariable userId: Long,
        @RequestBody request: PointDTOs.ReleasePointRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        userService.releasePoint(
            userId = userId,
            amount = request.amount,
            reason = request.reason,
            auctionId = request.auctionId
        )
        
        return ResponseEntity.ok(ApiResponse.ok())
    }
    
    @Operation(summary = "포인트 차감", description = "수수료 등 포인트 차감")
    @PostMapping("/{userId}/points/deduct")
    fun deductPoint(
        @PathVariable userId: Long,
        @RequestParam amount: Long,
        @RequestParam reason: String
    ): ResponseEntity<ApiResponse<Unit>> {
        userService.deductPoint(
            userId = userId,
            amount = amount,
            reason = reason
        )
        
        return ResponseEntity.ok(ApiResponse.ok())
    }
}

