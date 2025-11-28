package com.ch.auction.domain.repository

interface UserPointRepository {
    fun chargePoint(
        userId: Long,
        amount: Long
    ): Long

    fun usePoint(
        userId: Long,
        amount: Long
    )

    fun getPoint(
        userId: Long
    ): UserPointInfo
}

data class UserPointInfo(
    val totalPoint: Long,
    val lockedPoint: Long
) {
    val availablePoint: Long
        get() = totalPoint - lockedPoint
}
