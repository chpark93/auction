package com.ch.auction.user.domain

enum class TransactionStatus {
    // 대기 중 (HOLD 상태)
    PENDING,
    // 완료 (USE, RELEASE)
    COMPLETED,
    // 취소
    CANCELLED
}

