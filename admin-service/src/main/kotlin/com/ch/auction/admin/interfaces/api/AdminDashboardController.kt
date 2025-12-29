package com.ch.auction.admin.interfaces.api

import com.ch.auction.admin.application.service.AdminDashboardService
import com.ch.auction.admin.interfaces.api.dto.DashboardResponse
import com.ch.auction.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin Dashboard API", description = "관리자 대시보드 API")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
class AdminDashboardController(
    private val adminDashboardService: AdminDashboardService
) {

    @Operation(summary = "대시보드 통계 조회", description = "대시보드 통계 조회")
    @GetMapping("/stats")
    fun getDashboardStats(): ResponseEntity<ApiResponse<DashboardResponse>> {
        val stats = adminDashboardService.getDashboardStats()
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = stats
            )
        )
    }
}

