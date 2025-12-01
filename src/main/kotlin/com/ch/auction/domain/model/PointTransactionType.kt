package com.ch.auction.domain.model

enum class PointTransactionType {
    // 충전
    CHARGE,
    // 사용 (낙찰 시 차감)
    USE,
    // 환불
    REFUND,
    // 정산 (지급 완료)
    SETTLEMENT
}
