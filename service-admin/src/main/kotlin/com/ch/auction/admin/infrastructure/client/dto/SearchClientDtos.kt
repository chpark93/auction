package com.ch.auction.admin.infrastructure.client.dto

object SearchClientDtos {
    data class StatsResponse(
        val totalAuctions: Long,
        val ongoingAuctions: Long,
        val completedAuctions: Long,
        val totalUsers: Long,
        val totalSettlements: Long,
        val todayBids: Long
    )
}

