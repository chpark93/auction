package com.ch.auction.common.dto

object PointDTOs {
    data class PointRequest(
        val amount: Long,
        val transactionKey: String? = null,
        val orderId: String? = null
    )

    data class PointResponse(
        val userId: Long,
        val totalPoint: Long,
        val lockedPoint: Long = 0L,
        val availablePoint: Long
    )
    
    data class HoldPointRequest(
        val amount: Long,
        val reason: String,
        val auctionId: Long? = null
    )
    
    data class ReleasePointRequest(
        val amount: Long,
        val reason: String,
        val auctionId: Long? = null
    )
}

