package com.ch.auction.payment.domain

enum class DeliveryStatus {
    // 배송 준비
    PREPARING,
    // 배송 중
    SHIPPING,
    // 배송 완료
    DELIVERED,
    // 구매 확정
    CONFIRMED
}

