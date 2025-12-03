package com.ch.auction.user.application.service

import com.ch.auction.common.ErrorCode
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.common.event.UserUpdatedEvent
import com.ch.auction.exception.BusinessException
import com.ch.auction.user.infrastructure.persistence.UserRepository
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>
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

        user.chargePoint(
            amount = amount
        )
        
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

        user.usePoint(
            amount = amount
        )

        return PointDTOs.PointResponse(
            userId = user.id!!,
            totalPoint = user.point,
            availablePoint = user.point
        )
    }
    
    @Transactional
    fun updateProfile(
        userId: Long,
        nickname: String
    ) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
            
        user.updateProfile(nickname)
        
        kafkaTemplate.send("user-update", UserUpdatedEvent(
            userId = userId,
            nickname = nickname
        ))
    }
}
