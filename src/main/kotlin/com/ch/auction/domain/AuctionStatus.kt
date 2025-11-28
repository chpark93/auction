package com.ch.auction.domain

enum class AuctionStatus {
    PENDING,    // 검수 대기
    REJECTED,   // 승인 거절
    APPROVED,   // 승인 완료 (스케줄러 대기)

    READY,      // 시작 전 (관리자 직접 생성 등)
    ONGOING,    // 진행 중
    ENDED,      // 시간 종료 (입찰 마감)
    COMPLETED,  // 낙찰
    FAILED      // 유찰
}
