package com.ch.auction.user.interfaces.api

import com.ch.auction.common.ApiResponse
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.user.application.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/{userId}")
    fun getUser(
        @PathVariable userId: Long
    ): ResponseEntity<ApiResponse<PointDTOs.PointResponse>> {
        val response = userService.getUser(
            userId = userId
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }

    @GetMapping("/email")
    fun getUserByEmail(
        @RequestParam email: String
    ): ResponseEntity<ApiResponse<PointDTOs.PointResponse>> {
        val response = userService.getUserByEmail(
            email = email
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = response
            )
        )
    }

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
}
