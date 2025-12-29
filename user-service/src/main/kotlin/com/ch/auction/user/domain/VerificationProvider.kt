package com.ch.auction.user.domain

interface VerificationProvider {
    fun getInfo(
        impUid: String
    ): VerificationInfo
}

