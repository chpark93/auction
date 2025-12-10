package com.ch.auction.user.application.service

import com.ch.auction.common.ErrorCode
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.common.enums.UserStatus
import com.ch.auction.common.event.UserUpdatedEvent
import com.ch.auction.exception.BusinessException
import com.ch.auction.user.infrastructure.persistence.UserRepository
import com.ch.auction.user.interfaces.api.dto.UserListResponse
import com.ch.auction.user.interfaces.api.dto.UserResponse
import org.springframework.data.domain.PageRequest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    
    @Transactional(readOnly = true)
    fun getUsers(
        page: Int,
        size: Int,
        status: UserStatus?
    ): UserListResponse {
        val pageable = PageRequest.of(page, size)
        
        val usersPage = if (status != null) {
            userRepository.findAllByStatus(status, pageable)
        } else {
            userRepository.findAll(pageable)
        }
        
        val userResponses = usersPage.content.map { UserResponse.from(it) }
        
        return UserListResponse(
            content = userResponses,
            totalElements = usersPage.totalElements,
            totalPages = usersPage.totalPages,
            number = usersPage.number,
            size = usersPage.size
        )
    }
    
    @Transactional
    fun updateUserStatus(
        userId: Long,
        status: UserStatus,
        reason: String?
    ) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        user.updateStatus(status)
        
        // TODO: 상태 변경 이벤트 발행 (필요시)
    }
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
