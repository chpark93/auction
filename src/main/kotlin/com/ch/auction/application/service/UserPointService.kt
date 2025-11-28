package com.ch.auction.application.service

import com.ch.auction.domain.repository.UserPointRepository
import com.ch.auction.interfaces.api.dto.user.PointRequest
import com.ch.auction.interfaces.api.dto.user.PointResponse
import org.springframework.stereotype.Service

@Service
class UserPointService(
    private val userPointRepository: UserPointRepository
) {
    fun chargePoint(
        userId: Long,
        request: PointRequest
    ): PointResponse {
        userPointRepository.chargePoint(
            userId = userId,
            amount = request.amount
        )

        return getPoint(
            userId = userId
        )
    }

    fun getPoint(
        userId: Long
    ): PointResponse {
        val info = userPointRepository.getPoint(
            userId = userId
        )

        return PointResponse(
            userId = userId,
            totalPoint = info.totalPoint,
            lockedPoint = info.lockedPoint,
            availablePoint = info.availablePoint
        )
    }
}

