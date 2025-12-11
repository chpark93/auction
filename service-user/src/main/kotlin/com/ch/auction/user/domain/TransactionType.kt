package com.ch.auction.user.domain

enum class TransactionType {
    // 충전
    CHARGE,
    // 입찰 시 홀드 (차감)
    HOLD,
    // 유찰 시 환불
    RELEASE,
    // 낙찰 시 사용 확정
    USE,
    // 관리자 환불
    REFUND
}

