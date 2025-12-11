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
        
        val elasticsearchStatsFuture = CompletableFuture.supplyAsync({
            try {
                searchClient.getStats().data
            } catch (e: Exception) {
                logger.warn("Failed to fetch Elasticsearch stats: ${e.message}")
                null
            }
        }, virtualExecutor)
        
        // CompletableFuture 완료 대기
        CompletableFuture.allOf(
            usersFuture,
            pendingAuctionsFuture,
            settlementsFuture,
            elasticsearchStatsFuture
        ).join()
        
        val usersResponse = usersFuture.join()
        val pendingAuctionsResponse = pendingAuctionsFuture.join()
        val settlementsResponse = settlementsFuture.join()
        val esStats = elasticsearchStatsFuture.join()
        
        val elapsedTime = System.currentTimeMillis() - startTime
        logger.info("Fetched dashboard stats in ${elapsedTime}ms (Elasticsearch: primary source)")
        
        return DashboardResponse(
            totalUsers = usersResponse?.data?.totalElements ?: 0L,
            totalAuctions = esStats?.totalAuctions ?: 0L,
            pendingAuctions = pendingAuctionsResponse?.data?.totalElements ?: 0L,
            ongoingAuctions = esStats?.ongoingAuctions ?: 0L,
            completedAuctions = esStats?.completedAuctions ?: 0L,
            totalSettlements = settlementsResponse?.data?.totalElements ?: 0L,
            averageCurrentPrice = esStats?.averageCurrentPrice ?: 0.0,
            statusDistribution = esStats?.statusDistribution ?: emptyMap(),
            categoryDistribution = esStats?.categoryDistribution ?: emptyMap(),
            hourlyRegistrationTrend = esStats?.hourlyRegistrationTrend ?: emptyList(),
            todayBids = esStats?.todayBids ?: 0L
        )
    }
}

