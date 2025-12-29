package com.ch.auction.search.interfaces.api.dto

data class AuctionStatsResponse(
    val totalAuctions: Long,
    val ongoingAuctions: Long,
    val completedAuctions: Long,
    val statusDistribution: Map<String, Long>,
    val categoryDistribution: Map<String, Long>,
    val hourlyRegistrationTrend: List<HourlyTrend>,
    val averageCurrentPrice: Double,
    val todayBids: Long = 0
)

data class HourlyTrend(
    val hour: String,
    val count: Long
)

