package com.ch.auction.common.event

data class UserUpdatedEvent(
    val userId: Long,
    val nickname: String
)

