package com.ch.auction.admin.interfaces.api.dto

import com.ch.auction.admin.infrastructure.client.dto.SearchClientDtos

data class DashboardResponse(
    val totalUsers: Long,
    val totalAuctions: Long,
    val pendingAuctions: Long,
    val ongoingAuctions: Long,
    val completedAuctions: Long,
    val totalSettlements: Long,
    val averageCurrentPrice: Double,
    val statusDistribution: Map<String, Long>,
    val categoryDistribution: Map<String, Long>,
    val hourlyRegistrationTrend: List<SearchClientDtos.HourlyTrend>,
    val todayBids: Long
)

