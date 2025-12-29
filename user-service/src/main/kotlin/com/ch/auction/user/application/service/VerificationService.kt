package com.ch.auction.user.application.service

import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import com.ch.auction.user.application.dto.VerificationRequest
import com.ch.auction.user.domain.VerificationProvider
import com.ch.auction.user.infrastructure.persistence.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VerificationService(
    private val userRepository: UserRepository,
    private val verificationProvider: VerificationProvider
) {

    @Transactional
    fun completeVerification(
        email: String,
        request: VerificationRequest
    ) {
        val user = userRepository.findByEmail(
            email = email
        ).orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        if (user.isVerified) {
            throw BusinessException(ErrorCode.ALREADY_VERIFIED_USER)
        }

        val info = verificationProvider.getInfo(request.impUid)

        if (userRepository.existsByCi(ci = info.ci)) {
            throw BusinessException(ErrorCode.DUPLICATE_CI)
        }

        user.verifyIdentity(
            name = info.name,
            phoneNumber = info.phoneNumber,
            ci = info.ci
        )
    }
}