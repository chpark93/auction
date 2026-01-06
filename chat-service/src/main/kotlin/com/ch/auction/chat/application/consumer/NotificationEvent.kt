package com.ch.auction.chat.application.consumer

data class NotificationEvent(
    val userId: Long,
    val message: String,
    val type: NotificationType,
    val relatedId: Long? = null
)

enum class NotificationType {
    AUCTION_ENDED,
    AUCTION_FAILED,
    BID_SUCCESS,
    PAYMENT_COMPLETED,
    DELIVERY_STARTED
}

