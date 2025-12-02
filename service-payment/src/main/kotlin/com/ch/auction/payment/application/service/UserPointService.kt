package com.ch.auction.payment.application.service

import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.payment.domain.UserPointRepository
import org.springframework.stereotype.Service

@Service
class UserPointService(
    private val userPointRepository: UserPointRepository
) {
    fun chargePoint(
        userId: Long,
        request: PointDTOs.PointRequest
    ): PointDTOs.PointResponse {
        userPointRepository.chargePoint(
            userId = userId,
            amount = request.amount.toLong()
        )

        return getPoint(
            userId = userId
        )
    }

    fun getPoint(
        userId: Long
    ): PointDTOs.PointResponse {
        val info = userPointRepository.getPoint(
            userId = userId
        )

        return PointDTOs.PointResponse(
            userId = userId,
            totalPoint = info.totalPoint,
            lockedPoint = info.lockedPoint,
            availablePoint = info.availablePoint
        )
    }
}
