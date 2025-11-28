package com.ch.auction.application.service

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.domain.verification.VerificationProvider
import com.ch.auction.infrastructure.persistence.UserRepository
import com.ch.auction.interfaces.api.dto.user.VerificationRequest
import com.ch.auction.interfaces.common.ErrorCode
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
