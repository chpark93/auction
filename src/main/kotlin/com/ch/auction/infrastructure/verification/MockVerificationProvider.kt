package com.ch.auction.infrastructure.verification

import com.ch.auction.domain.verification.VerificationInfo
import com.ch.auction.domain.verification.VerificationProvider
import org.springframework.stereotype.Component
import java.util.*

@Component
class MockVerificationProvider : VerificationProvider {

    override fun getInfo(
        impUid: String
    ): VerificationInfo {

        return VerificationInfo(
            name = "홍길동",
            phoneNumber = "01012345678",
            ci = "mock-ci-${UUID.randomUUID()}"
        )
    }
}

