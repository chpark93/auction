package com.ch.auction.user.interfaces.api

import com.ch.auction.common.ApiResponse
import com.ch.auction.common.enums.UserStatus
import com.ch.auction.user.application.service.UserService
import com.ch.auction.user.interfaces.api.dto.UpdateStatusRequest
import com.ch.auction.user.interfaces.api.dto.UserListResponse
import com.ch.auction.user.interfaces.api.dto.UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "User API", description = "회원 관리 API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    @Operation(summary = "회원 목록 조회", description = "회원 목록을 페이징하여 조회합니다")
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
            ApiResponse.ok(data = response)
        )
    }
    
    @Operation(summary = "회원 상세 조회", description = "특정 회원의 상세 정보를 조회합니다")
    @GetMapping("/{userId}")
    fun getUser(
        @PathVariable userId: Long
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userService.getUser(userId)
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = UserResponse(
                    userId = user.userId,
                    email = "",
                    nickname = "",
                    name = "",
                    phoneNumber = "",
                    role = com.ch.auction.common.enums.UserRole.ROLE_USER,
                    status = UserStatus.ACTIVE,
                    totalPoint = user.totalPoint,
                    createdAt = java.time.LocalDateTime.now()
                )
            )
        )
    }
    
    @Operation(summary = "회원 상태 변경", description = "회원의 상태를 변경합니다 (차단/해제)")
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
}
