package com.ch.auction.admin.application.service

import com.ch.auction.admin.infrastructure.client.AuctionClient
import com.ch.auction.admin.infrastructure.client.UserClient
import com.ch.auction.admin.infrastructure.client.dto.AuctionClientDtos
import com.ch.auction.admin.interfaces.api.dto.AdminAuctionListResponse
import com.ch.auction.admin.interfaces.api.dto.AdminAuctionResponse
import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AdminAuctionService(
    private val auctionClient: AuctionClient,
    private val userClient: UserClient
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 경매 목록 조회
     */
    fun getAuctions(
        page: Int = 0,
        size: Int = 20,
        status: String?
    ): AuctionClientDtos.AuctionListResponse {
        val response = auctionClient.getAuctions(
            page = page,
            size = size,
            status = status
        )
        
        return response.data ?: AuctionClientDtos.AuctionListResponse(
            content = emptyList(),
            totalElements = 0,
            totalPages = 0,
            number = page,
            size = size
        )
    }
    
    /**
     * 경매 목록 조회 Batch API
     */
    fun getAuctionsWithSellerInfo(
        page: Int = 0,
        size: Int = 20,
        status: String?
    ): AdminAuctionListResponse {
        val startTime = System.currentTimeMillis()
        
        // 경매 목록 조회
        val auctionsResponse = auctionClient.getAuctions(
            page = page,
            size = size,
            status = status
        )
        
        val auctions = auctionsResponse.data?.content ?: emptyList()
        
        if (auctions.isEmpty()) {
            logger.info("No auctions found")
            return AdminAuctionListResponse(
                content = emptyList(),
                totalElements = 0,
                totalPages = 0,
                number = page,
                size = size
            )
        }
        
        val sellerIds = auctions.map { it.sellerId }.distinct()
        logger.info("Fetched ${auctions.size} auctions, fetching ${sellerIds.size} unique sellers using Batch API")

        val sellerInfoMap = try {
            val batchResponse = userClient.getUsersBatch(
                ids = sellerIds
            )
            batchResponse.data ?: emptyMap()
        } catch (e: Exception) {
            logger.error("Failed to fetch sellers in batch: ${e.message}", e)
            emptyMap()
        }
        
        val enrichedAuctions = auctions.map { auction ->
            val sellerInfo = sellerInfoMap[auction.sellerId]
            
            AdminAuctionResponse(
                id = auction.id,
                title = auction.title,
                sellerId = auction.sellerId,
                sellerEmail = sellerInfo?.email,
                sellerName = sellerInfo?.nickname ?: auction.sellerName,
                startPrice = auction.startPrice,
                currentPrice = auction.currentPrice,
                status = auction.status,
                startTime = auction.startTime,
                endTime = auction.endTime,
                createdAt = auction.createdAt,
                bidCount = 0, // TODO: Redis에서 입찰 수 조회
                reportCount = 0 // TODO: 신고 서비스에서 조회
            )
        }
        
        val elapsedTime = System.currentTimeMillis() - startTime
        logger.info("✅ Fetched ${auctions.size} auctions with seller info in ${elapsedTime}ms using Batch API (1 request for ${sellerIds.size} users)")
        
        return AdminAuctionListResponse(
            content = enrichedAuctions,
            totalElements = auctionsResponse.data?.totalElements ?: 0L,
            totalPages = auctionsResponse.data?.totalPages ?: 0,
            number = auctionsResponse.data?.number ?: page,
            size = auctionsResponse.data?.size ?: size
        )
    }
    
    /**
     * 승인 대기 경매 목록 조회
     */
    fun getPendingAuctions(
        page: Int = 0,
        size: Int = 20
    ): AuctionClientDtos.AuctionListResponse {
        return getAuctions(
            page = page,
            size = size,
            status = "PENDING"
        )
    }
    
    /**
     * 경매 상세 조회
     */
    fun getAuction(
        auctionId: Long
    ): AuctionClientDtos.AuctionResponse {
        val response = auctionClient.getAuction(
            auctionId = auctionId
        )

        return response.data ?: throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)
    }
    
    /**
     * 경매 승인
     */
    fun approveAuction(
        auctionId: Long
    ) {
        auctionClient.approveAuction(
            auctionId = auctionId
        )
    }
    
    /**
     * 경매 거절
     */
    fun rejectAuction(
        auctionId: Long,
        reason: String
    ) {
        auctionClient.rejectAuction(
            auctionId = auctionId,
            request = AuctionClientDtos.RejectRequest(
                reason = reason
            )
        )
    }
    
    /**
     * 경매 삭제
     */
    fun deleteAuction(
        auctionId: Long
    ) {
        auctionClient.deleteAuction(
            auctionId = auctionId
        )
    }
    
    /**
     * 경매 강제 시작
     */
    fun startAuction(
        auctionId: Long
    ) {
        auctionClient.startAuction(
            auctionId = auctionId
        )
    }
    
    /**
     * 경매 강제 종료
     */
    fun endAuction(
        auctionId: Long
    ) {
        auctionClient.endAuction(
            auctionId = auctionId
        )
    }
}

