package com.ch.auction.interfaces.api

import com.ch.auction.application.service.UserPointService
import com.ch.auction.interfaces.api.dto.user.PointRequest
import com.ch.auction.interfaces.api.dto.user.PointResponse
import com.ch.auction.interfaces.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users/points")
class UserPointController(
    private val userPointService: UserPointService
) {

    @PostMapping("/charge")
    fun chargePoint(
        // TODO: jwt 토큰 방식으로 변경 예정
        @RequestHeader("X-User-Id") userId: Long,
        @RequestBody request: PointRequest
    ): ResponseEntity<ApiResponse<PointResponse>> {
        val response = userPointService.chargePoint(
            userId = userId,
            request = request
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }

    @GetMapping("/me")
    fun getPoint(
        // TODO: jwt 토큰 방식으로 변경 예정
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<ApiResponse<PointResponse>> {
        val response = userPointService.getPoint(
            userId = userId
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }
}

