package com.ch.auction.domain

enum class AuctionStatus {
    READY,      // 시작 전
    ONGOING,    // 진행 중
    ENDED,      // 시간 종료 (입찰 마감)
    COMPLETED,  // 낙찰 완료 (정산 완료)
    FAILED      // 유찰
}
