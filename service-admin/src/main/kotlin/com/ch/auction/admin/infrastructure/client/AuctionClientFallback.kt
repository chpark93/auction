package com.ch.auction.admin.infrastructure.client

import com.ch.auction.admin.infrastructure.client.dto.AuctionClientDtos
import com.ch.auction.common.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AuctionClientFallback : AuctionClient {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getAuctions(
        page: Int?,
        size: Int?,
        status: String?
    ): ApiResponse<AuctionClientDtos.AuctionListResponse> {
        logger.error("AuctionClient fallback triggered for getAuctions")
        throw RuntimeException("Auction service is unavailable")
    }

    override fun getAuction(
        auctionId: Long
    ): ApiResponse<AuctionClientDtos.AuctionResponse> {
        logger.error("AuctionClient fallback triggered for getAuction: auctionId=$auctionId")
        throw RuntimeException("Auction service is unavailable")
    }

    override fun approveAuction(
        auctionId: Long
    ): ApiResponse<Unit> {
        logger.error("AuctionClient fallback triggered for approveAuction: auctionId=$auctionId")
        throw RuntimeException("Auction service is unavailable. Cannot approve auction.")
    }

    override fun rejectAuction(
        auctionId: Long,
        request: AuctionClientDtos.RejectRequest
    ): ApiResponse<Unit> {
        logger.error("AuctionClient fallback triggered for rejectAuction: auctionId=$auctionId")
        throw RuntimeException("Auction service is unavailable. Cannot reject auction.")
    }

    override fun deleteAuction(
        auctionId: Long
    ): ApiResponse<Unit> {
        logger.error("AuctionClient fallback triggered for deleteAuction: auctionId=$auctionId")
        throw RuntimeException("Auction service is unavailable. Cannot delete auction.")
    }

    override fun startAuction(
        auctionId: Long
    ): ApiResponse<Unit> {
        logger.error("AuctionClient fallback triggered for startAuction: auctionId=$auctionId")
        throw RuntimeException("Auction service is unavailable. Cannot start auction.")
    }

    override fun endAuction(
        auctionId: Long
    ): ApiResponse<Unit> {
        logger.error("AuctionClient fallback triggered for endAuction: auctionId=$auctionId")
        throw RuntimeException("Auction service is unavailable. Cannot end auction.")
    }
}

