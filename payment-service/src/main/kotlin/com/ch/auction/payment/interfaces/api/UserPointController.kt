package com.ch.auction.payment.interfaces.api

import com.ch.auction.common.ApiResponse
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.payment.application.service.UserPointService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users/points")
class UserPointController(
    private val userPointService: UserPointService
) {

    @PostMapping("/charge")
    fun chargePoint(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestBody request: PointDTOs.PointRequest
    ): ResponseEntity<ApiResponse<PointDTOs.PointResponse>> {
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
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<ApiResponse<PointDTOs.PointResponse>> {
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
