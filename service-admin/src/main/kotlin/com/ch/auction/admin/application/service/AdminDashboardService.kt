package com.ch.auction.admin.application.service

import com.ch.auction.admin.infrastructure.client.AuctionClient
import com.ch.auction.admin.infrastructure.client.PaymentClient
import com.ch.auction.admin.infrastructure.client.SearchClient
import com.ch.auction.admin.infrastructure.client.UserClient
import com.ch.auction.admin.interfaces.api.dto.DashboardResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

@Service
class AdminDashboardService(
    private val userClient: UserClient,
    private val auctionClient: AuctionClient,
    private val paymentClient: PaymentClient,
    private val searchClient: SearchClient
) {
    private val virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * 대시보드 통계 조회
     * 각각 서비스의 데이터를 병렬로 조회하여 조합
     */
    fun getDashboardStats(): DashboardResponse {
        val startTime = System.currentTimeMillis()
        
        val usersFuture = CompletableFuture.supplyAsync({
            try {
                userClient.getUsers(
                    page = 0,
                    size = 1,
                    status = null
                )
            } catch (e: Exception) {
                logger.warn("Failed to fetch users stats: ${e.message}")
                null
            }
        }, virtualExecutor)
        
        val auctionsFuture = CompletableFuture.supplyAsync({
            try {
                auctionClient.getAuctions(
                    page = 0,
                    size = 1,
                    status = null
                )
            } catch (e: Exception) {
                logger.warn("Failed to fetch auctions stats: ${e.message}")
                null
            }
        }, virtualExecutor)
        
        val pendingAuctionsFuture = CompletableFuture.supplyAsync({
            try {
                auctionClient.getAuctions(
                    page = 0,
                    size = 1,
                    status = "PENDING"
                )
            } catch (e: Exception) {
                logger.warn("Failed to fetch pending auctions stats: ${e.message}")
                null
            }
        }, virtualExecutor)
        
        val settlementsFuture = CompletableFuture.supplyAsync({
            try {
                paymentClient.getSettlements(
                    page = 0,
                    size = 1,
                    status = null
                )
            } catch (e: Exception) {
                logger.warn("Failed to fetch settlements stats: ${e.message}")
                null
            }
        }, virtualExecutor)
        
        val searchStatsFuture = CompletableFuture.supplyAsync({
            try {
                searchClient.getStats().data
            } catch (e: Exception) {
                logger.warn("Failed to fetch search stats: ${e.message}")
                null
            }
        }, virtualExecutor)
        
        // CompletableFuture 완료 대기
        CompletableFuture.allOf(
            usersFuture,
            auctionsFuture,
            pendingAuctionsFuture,
            settlementsFuture,
            searchStatsFuture
        ).join()
        
        // 조합
        val usersResponse = usersFuture.join()
        val auctionsResponse = auctionsFuture.join()
        val pendingAuctionsResponse = pendingAuctionsFuture.join()
        val settlementsResponse = settlementsFuture.join()
        val searchStats = searchStatsFuture.join()
        
        val elapsedTime = System.currentTimeMillis() - startTime
        logger.info("Fetched dashboard stats in ${elapsedTime}ms using Virtual Threads (5 parallel calls)")
        
        return DashboardResponse(
            totalUsers = usersResponse?.data?.totalElements ?: 0L,
            totalAuctions = auctionsResponse?.data?.totalElements ?: 0L,
            pendingAuctions = pendingAuctionsResponse?.data?.totalElements ?: 0L,
            totalSettlements = settlementsResponse?.data?.totalElements ?: 0L,
            ongoingAuctions = searchStats?.ongoingAuctions ?: 0L,
            todayBids = searchStats?.todayBids ?: 0L
        )
    }
}

