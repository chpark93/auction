package com.ch.auction.domain.verification

interface VerificationProvider {
    fun getInfo(
        impUid: String
    ): VerificationInfo
}

