package com.ch.auction.admin.interfaces.api.dto

data class DashboardResponse(
    val totalUsers: Long,
    val totalAuctions: Long,
    val pendingAuctions: Long,
    val ongoingAuctions: Long,
    val totalSettlements: Long,
    val todayBids: Long
)

