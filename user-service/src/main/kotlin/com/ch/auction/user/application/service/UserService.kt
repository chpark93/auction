package com.ch.auction.user.application.service

import com.ch.auction.common.ErrorCode
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.common.enums.UserStatus
import com.ch.auction.common.event.UserUpdatedEvent
import com.ch.auction.exception.BusinessException
import com.ch.auction.user.domain.PointTransaction
import com.ch.auction.user.domain.TransactionStatus
import com.ch.auction.user.domain.TransactionType
import com.ch.auction.user.infrastructure.persistence.PointTransactionRepository
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
    private val pointTransactionRepository: PointTransactionRepository,
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
            userRepository.findAllByStatus(
                status = status,
                pageable = pageable
            )
        } else {
            userRepository.findAll(pageable)
        }
        
        val userResponses = usersPage.content.map {
            UserResponse.from(
                user = it
            )
        }
        
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
        
        // TODO: 상태 변경 이벤트 발행
    }

    @Transactional(readOnly = true)
    fun getUser(
        userId: Long
    ): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        return UserResponse.from(user)
    }
    
    @Transactional(readOnly = true)
    fun getUserPoint(
        userId: Long,
        auctionId: Long? = null
    ): PointDTOs.PointResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        // 실제 PENDING 트랜잭션 기반으로 lockedPoint 계산
        val actualLockedPoint = pointTransactionRepository
            .findByUserIdAndStatusAndType(
                userId = userId,
                status = TransactionStatus.PENDING,
                type = TransactionType.HOLD
            )
            .sumOf { it.amount }
        
        // 재입찰인 경우: 해당 경매의 기존 HOLD 금액을 제외한 available point 계산
        val adjustedAvailable = if (auctionId != null) {
            val existingHolds = pointTransactionRepository.findByAuctionIdAndUserIdAndStatusAndType(
                auctionId = auctionId,
                userId = userId,
                status = TransactionStatus.PENDING,
                type = TransactionType.HOLD
            )
            val existingHoldAmount = existingHolds.sumOf { it.amount }

            (user.point - actualLockedPoint) + existingHoldAmount  // 기존 입찰 금액을 더해서 반환
        } else {
            user.point - actualLockedPoint
        }
        
        return PointDTOs.PointResponse(
            userId = user.id!!,
            totalPoint = user.point,
            lockedPoint = actualLockedPoint,
            availablePoint = adjustedAvailable
        )
    }

    @Transactional(readOnly = true)
    fun getUsersBatch(
        userIds: List<Long>
    ): Map<Long, UserResponse> {
        if (userIds.isEmpty()) {
            return emptyMap()
        }
        
        val users = userRepository.findAllById(userIds)
        
        return users.associate { user ->
            user.id!! to UserResponse.from(
                user = user
            )
        }
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
    
    @Transactional(readOnly = true)
    fun getUserByEmailAsUserResponse(
        email: String
    ): UserResponse {
        val user = userRepository.findByEmail(
            email = email
        ).orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        return UserResponse.from(user)
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
        
        // 충전 트랜잭션 기록
        pointTransactionRepository.save(
            PointTransaction.create(
                userId = userId,
                type = TransactionType.CHARGE,
                amount = amount,
                balanceAfter = user.point,
                reason = "포인트 충전",
                status = TransactionStatus.COMPLETED
            )
        )
        
        return PointDTOs.PointResponse(
            userId = user.id!!,
            totalPoint = user.point,
            lockedPoint = user.lockedPoint,
            availablePoint = user.availablePoint
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
            lockedPoint = user.lockedPoint,
            availablePoint = user.availablePoint
        )
    }
    
    @Transactional(readOnly = true)
    fun getUserPointAmount(
        userId: Long
    ): Long {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        return user.point
    }
    
    @Transactional
    fun holdPoint(
        userId: Long,
        amount: Long,
        reason: String,
        auctionId: Long? = null
    ) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        // 재입찰인 경우: 같은 경매의 기존 HOLD 트랜잭션 해제
        if (auctionId != null) {
            val existingHolds = pointTransactionRepository.findByAuctionIdAndUserIdAndStatusAndType(
                auctionId = auctionId,
                userId = userId,
                status = TransactionStatus.PENDING,
                type = TransactionType.HOLD
            )
            
            existingHolds.forEach { existingHold ->
                // 기존 lock 해제
                user.releasePoint(existingHold.amount)
                existingHold.cancel()
            }
        }
        
        // 새로운 금액으로 포인트 홀드 (lockedPoint만 증가, point는 유지)
        user.holdPoint(
            amount = amount
        )
        
        // HOLD 트랜잭션 (PENDING)
        pointTransactionRepository.save(
            PointTransaction.create(
                userId = userId,
                type = TransactionType.HOLD,
                amount = amount,
                balanceAfter = user.point,
                reason = reason,
                auctionId = auctionId,
                status = TransactionStatus.PENDING
            )
        )
    }
    
    @Transactional
    fun releasePoint(
        userId: Long,
        amount: Long,
        reason: String,
        auctionId: Long? = null
    ) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        // HOLD 트랜잭션 완료
        if (auctionId != null) {
            val holdTransactions = pointTransactionRepository.findByAuctionIdAndUserIdAndStatusAndType(
                auctionId = auctionId,
                userId = userId,
                status = TransactionStatus.PENDING,
                type = TransactionType.HOLD
            )

            holdTransactions.forEach { it.complete() }
        }
        
        // 유찰 -> 포인트 lock 해제 (lockedPoint만 감소, point는 유지)
        user.releasePoint(
            amount = amount
        )
        
        // RELEASE 트랜잭션 (COMPLETED)
        pointTransactionRepository.save(
            PointTransaction.create(
                userId = userId,
                type = TransactionType.RELEASE,
                amount = amount,
                balanceAfter = user.point,
                reason = reason,
                auctionId = auctionId,
                status = TransactionStatus.COMPLETED
            )
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
    
    /**
     * 포인트 직접 차감 (수수료 등)
     * lockedPoint와 무관 -> 실제 포인트 차감
     */
    @Transactional
    fun deductPoint(
        userId: Long,
        amount: Long,
        reason: String
    ) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        user.usePoint(
            amount = amount
        )
        
        // USE 트랜잭션
        pointTransactionRepository.save(
            PointTransaction.create(
                userId = userId,
                type = TransactionType.USE,
                amount = amount,
                balanceAfter = user.point,
                reason = reason,
                status = TransactionStatus.COMPLETED
            )
        )
    }
}
