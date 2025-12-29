package com.ch.auction.auction.domain

enum class BidStatus {
    // 활성 입찰
    ACTIVE,
    // 포기(취소)
    CANCELLED,
    // 밀림
    OUTBID
}

