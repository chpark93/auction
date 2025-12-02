package com.ch.auction.user.application.service

import com.ch.auction.common.ErrorCode
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.exception.BusinessException
import com.ch.auction.user.infrastructure.persistence.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun getUser(
        userId: Long
    ): PointDTOs.PointResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        return PointDTOs.PointResponse(
            userId = user.id!!,
            totalPoint = user.point,
            availablePoint = user.point
        )
    }

    @Transactional(readOnly = true)
    fun getUserByEmail(
        email: String
    ): PointDTOs.PointResponse {
        val user = userRepository.findByEmail(
            email = email
        ).orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        return PointDTOs.PointResponse(
            userId = user.id!!,
            totalPoint = user.point,
            availablePoint = user.point
        )
    }

    @Transactional
    fun chargePoint(
        userId: Long,
        amount: Long
    ): PointDTOs.PointResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        user.chargePoint(amount)
        
        return PointDTOs.PointResponse(
            userId = user.id!!,
            totalPoint = user.point,
            availablePoint = user.point
        )
    }

    @Transactional
    fun usePoint(
        userId: Long,
        amount: Long
    ): PointDTOs.PointResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        user.usePoint(amount)

        return PointDTOs.PointResponse(
            userId = user.id!!,
            totalPoint = user.point,
            availablePoint = user.point
        )
    }
}
