package com.ch.auction.admin.interfaces.api.dto

import java.time.LocalDateTime

/**
 * 관리자용 경매 상세 응답 (판매자 정보 포함)
 */
data class AdminAuctionResponse(
    val id: Long,
    val title: String,
    val sellerId: Long,
    val sellerEmail: String?,
    val sellerName: String?,
    val startPrice: Long,
    val currentPrice: Long,
    val status: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val createdAt: LocalDateTime,
    val bidCount: Int = 0,
    val reportCount: Int = 0
)

/**
 * 관리자용 경매 목록 응답
 */
data class AdminAuctionListResponse(
    val content: List<AdminAuctionResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int
)

