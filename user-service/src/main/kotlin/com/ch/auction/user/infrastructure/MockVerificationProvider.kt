package com.ch.auction.user.infrastructure

import com.ch.auction.user.domain.VerificationInfo
import com.ch.auction.user.domain.VerificationProvider
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