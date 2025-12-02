package com.ch.auction.auction.domain

enum class AuctionStatus {
    // 대기
    PENDING,
    // 승인 거절
    REJECTED,
    // 승인 완료
    APPROVED,
    // 시작 전 (관리자 직접 생성)
    READY,
    // 진행 중
    ONGOING,
    // 종료 (입찰 마감)
    ENDED,
    // 낙찰
    COMPLETED,
    // 유찰
    FAILED
}
