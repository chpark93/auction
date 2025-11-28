package com.ch.auction.interfaces.api.dto.user

data class PointResponse(
    val userId: Long,
    val totalPoint: Long,
    val lockedPoint: Long,
    val availablePoint: Long
)

