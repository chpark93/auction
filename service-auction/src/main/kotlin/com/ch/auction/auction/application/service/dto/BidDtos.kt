package com.ch.auction.auction.application.service.dto

object BidDtos {
    data class CancelPolicy(
        val allowed: Boolean,
        val fee: Long,
        val message: String
    )

    data class CancelBidResult(
        val success: Boolean,
        val refundAmount: Long,
        val fee: Long,
        val reason: String
    )
}