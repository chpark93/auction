package com.ch.auction.admin.interfaces.api

import com.ch.auction.admin.application.service.AdminUserService
import com.ch.auction.admin.infrastructure.client.dto.UserClientDtos
import com.ch.auction.common.ApiResponse
import com.ch.auction.common.enums.UserStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Admin User API", description = "관리자 회원 관리 API")
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
class AdminUserController(
    private val adminUserService: AdminUserService
) {

    @Operation(summary = "회원 목록 조회", description = "전체 회원 목록 조회")
    @GetMapping
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: UserStatus?
    ): ResponseEntity<ApiResponse<UserClientDtos.UserListResponse>> {
        val response = adminUserService.getUsers(
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
    
    @Operation(summary = "회원 상세 조회", description = "회원 상세 정보 조회")
    @GetMapping("/{userId}")
    fun getUser(
        @PathVariable userId: Long
    ): ResponseEntity<ApiResponse<UserClientDtos.UserResponse>> {
        val response = adminUserService.getUser(
            userId = userId
        )
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }
    
    @Operation(summary = "회원 차단", description = "회원 차단")
    @PostMapping("/{userId}/ban")
    fun banUser(
        @PathVariable userId: Long,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<ApiResponse<Unit>> {
        val reason = request["reason"] ?: "관리자에 의한 차단"
        
        adminUserService.banUser(
            userId = userId,
            reason = reason
        )
        
        return ResponseEntity.ok(ApiResponse.ok())
    }
    
    @Operation(summary = "회원 차단 해제", description = "회원 차단 해제")
    @PostMapping("/{userId}/unban")
    fun unbanUser(
        @PathVariable userId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        adminUserService.unbanUser(
            userId = userId
        )
        
        return ResponseEntity.ok(ApiResponse.ok())
    }
}

