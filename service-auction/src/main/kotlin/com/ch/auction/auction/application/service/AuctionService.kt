package com.ch.auction.auction.application.service

import com.ch.auction.auction.domain.AuctionRepository
import com.ch.auction.auction.domain.AuctionStatus
import com.ch.auction.auction.domain.BidResult
import com.ch.auction.auction.infrastructure.client.user.UserClient
import com.ch.auction.auction.infrastructure.client.user.dtos.UserClientDtos
import com.ch.auction.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.auction.infrastructure.persistence.BidJpaRepository
import com.ch.auction.auction.infrastructure.redis.SellerInfoCacheRepository
import com.ch.auction.auction.infrastructure.redis.UserStatusCacheRepository
import com.ch.auction.auction.interfaces.api.dto.AuctionListResponse
import com.ch.auction.auction.interfaces.api.dto.AuctionResponse
import com.ch.auction.auction.interfaces.api.dto.BidHistoryResponse
import com.ch.auction.auction.interfaces.api.dto.CurrentPriceResponse
import com.ch.auction.common.ErrorCode
import com.ch.auction.common.enums.UserStatus
import com.ch.auction.exception.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

@Service
class AuctionService(
    private val auctionRepository: AuctionRepository,
    private val auctionJpaRepository: AuctionJpaRepository,
    private val bidJpaRepository: BidJpaRepository,
    private val sellerInfoCacheRepository: SellerInfoCacheRepository,
    private val userStatusCacheRepository: UserStatusCacheRepository,
    private val userClient: UserClient,
    private val bidCancellationService: BidCancellationService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 입찰
     */
    fun placeBid(
        auctionId: Long,
        userId: Long,
        amount: Long
    ): Long {
        val auction = auctionJpaRepository.findById(auctionId)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }
        
        if (auction.status != AuctionStatus.ONGOING) {
            throw BusinessException(ErrorCode.AUCTION_NOT_ONGOING)
        }
        
        val redisInfo = auctionRepository.getAuctionRedisInfo(auctionId)
        if (redisInfo == null) {
            logger.warn("Auction $auctionId not in Redis, loading now...")
            auctionRepository.loadAuctionToRedis(auctionId)
        }
        
        verifyUserStatus(
            userId = userId
        )
        
        // 사용 가능한 포인트 조회
        val userPoint = try {
            val response = userClient.getUserPoint(
                userId = userId,
                auctionId = auctionId  // 재입찰인 경우 기존 금액 제외
            )
            val pointInfo = response.data
            logger.info("User $userId point info for auction $auctionId - Total: ${pointInfo?.totalPoint}, Locked: ${pointInfo?.lockedPoint}, Available: ${pointInfo?.availablePoint}")
            pointInfo?.availablePoint ?: 0L
        } catch (e: Exception) {
            logger.error("Failed to get user point", e)
            throw BusinessException(ErrorCode.USER_SERVICE_UNAVAILABLE)
        }
        
        logger.info("User $userId attempting to bid $amount with available point $userPoint")

        val requestTime = Instant.now().toEpochMilli()

        val result = auctionRepository.tryBid(
            auctionId = auctionId,
            userId = userId,
            amount = amount,
            requestTime = requestTime,
            userPoint = userPoint
        )

        return when (result) {
            is BidResult.Success -> {
                // 입찰 성공 시 포인트 홀드
                try {
                    userClient.holdPoint(
                        userId = userId,
                        request = UserClientDtos.HoldPointRequest(
                            amount = amount,
                            reason = "경매 입찰 (경매 ID: $auctionId)",
                            auctionId = auctionId
                        )
                    )
                } catch (e: Exception) {
                    logger.error("Failed to hold point for user $userId", e)
                    // 포인트 홀드 실패 시에도 입찰은 성공으로 처리
                }
                result.currentPrice
            }
            is BidResult.PriceTooLow -> throw BusinessException(ErrorCode.PRICE_TOO_LOW)
            is BidResult.AuctionEnded -> throw BusinessException(ErrorCode.AUCTION_ENDED)
            is BidResult.AuctionNotFound -> throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)
            is BidResult.SelfBidding -> throw BusinessException(ErrorCode.SELF_BIDDING_NOT_ALLOWED)
            is BidResult.NotEnoughPoint -> throw BusinessException(ErrorCode.NOT_ENOUGH_POINT)
        }
    }

    fun getAuctionCurrentPrice(
        auctionId: Long
    ): CurrentPriceResponse {
        // Redis 정보 조회
        var info = auctionRepository.getAuctionRedisInfo(
            auctionId = auctionId
        )

        if (info == null) {
            auctionRepository.loadAuctionToRedis(
                auctionId = auctionId
            )

            info = auctionRepository.getAuctionRedisInfo(
                auctionId = auctionId
            ) ?: throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)
        }

        return CurrentPriceResponse(
            currentPrice = info.currentPrice,
            uniqueBidders = info.uniqueBidders,
            bidCount = info.bidCount
        )
    }

    fun getAuction(
        auctionId: Long
    ): AuctionResponse {
        val auction = auctionJpaRepository.findByIdOrNull(auctionId)
            ?: throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)

        val sellerName = getSellerName(
            userId = auction.sellerId
        )

        val redisInfo = auctionRepository.getAuctionRedisInfo(auctionId)

        return AuctionResponse.from(
            auction = auction,
            sellerName = sellerName,
            uniqueBidders = redisInfo?.uniqueBidders ?: 0,
            bidCount = redisInfo?.bidCount ?: 0
        )
    }

    fun getAuctions(
        pageable: Pageable
    ): Page<AuctionListResponse> {
        val statuses = listOf(
            AuctionStatus.ONGOING,
            AuctionStatus.APPROVED
        )
        
        val auctions = auctionJpaRepository.findByStatusInOrderByCreatedAtDesc(
            statuses = statuses,
            pageable = pageable
        )

        return auctions.map { auction ->
            val sellerName = getSellerName(auction.sellerId)
            val redisInfo = auctionRepository.getAuctionRedisInfo(auction.id!!)
            
            AuctionListResponse.from(
                auction = auction,
                sellerName = sellerName,
                currentPrice = redisInfo?.currentPrice ?: auction.currentPrice,
                uniqueBidders = redisInfo?.uniqueBidders ?: 0
            )
        }
    }

    private fun getSellerName(
        userId: Long
    ): String {
        val cachedName = sellerInfoCacheRepository.getSellerNickName(
            userId = userId
        )

        if (cachedName != null) {
            return cachedName
        }

        // Cache Miss -> Feign Client 호출
        try {
            val response = userClient.getUserInfo(
                userId = userId
            )
            val nickname = response.data?.nickname ?: "Unknown"

            // Cache Update
            sellerInfoCacheRepository.saveSellerNickName(
                userId = userId,
                nickname = nickname
            )

            return nickname
        } catch (_: Exception) {
            return "Unknown user"
        }
    }
    
    private fun verifyUserStatus(
        userId: Long
    ) {
        val cachedStatus = userStatusCacheRepository.getUserStatus(
            userId = userId
        )
        if (cachedStatus != null) {
            if (cachedStatus != UserStatus.ACTIVE.name) {
                 throw BusinessException(ErrorCode.USER_NOT_ACTIVE)
            }

            return
        }

        // Cache Miss -> Feign Client 호출
        try {
            val response = userClient.getUserInfo(
                userId = userId
            )
            val user = response.data ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
            
            if (user.status != UserStatus.ACTIVE) {
                userStatusCacheRepository.saveUserStatus(
                    userId = userId, 
                    status = user.status.name
                )

                throw BusinessException(ErrorCode.USER_NOT_ACTIVE)
            }
            
            // Cache Update
            userStatusCacheRepository.saveUserStatus(
                userId = userId,
                status = user.status.name
            )
            
        } catch (e: BusinessException) {
            throw e
        } catch (_: Exception) {
            throw BusinessException(ErrorCode.USER_SERVICE_UNAVAILABLE)
        }
    }

    /**
     * 경매 입찰 내역 조회
     */
    fun getBidHistory(
        auctionId: Long,
        limit: Int = 20
    ): List<BidHistoryResponse> {
        val bids = bidJpaRepository.findByAuctionIdOrderByBidTimeDesc(
            auctionId = auctionId,
            pageable = PageRequest.of(0, limit)
        )

        if (bids.isEmpty()) {
            return emptyList()
        }

        val userIds = bids.map { it.userId }.distinct()

        val usersMap = try {
            val response = userClient.getUsersBatch(
                userIds = userIds
            )
            response.data?.associateBy { it.id } ?: emptyMap()
        } catch (e: Exception) {
            logger.error("Failed to fetch users for bid history", e)
            emptyMap()
        }

        return bids.map { bid ->
            val user = usersMap[bid.userId]
            val userEmail = user?.email ?: "Unknown"
            val userNickname = user?.nickname

            BidHistoryResponse.from(
                bid = bid,
                userEmail = userEmail,
                userNickname = userNickname
            )
        }
    }
    
    /**
     * 입찰 포기
     */
    fun cancelBid(
        auctionId: Long,
        userId: Long,
        reason: String?
    ): Map<String, Any> {
        val result = bidCancellationService.cancelBid(
            auctionId = auctionId,
            userId = userId,
            reason = reason
        )
        
        return mapOf(
            "success" to result.success,
            "refundAmount" to result.refundAmount,
            "fee" to result.fee,
            "reason" to result.reason
        )
    }
    
    /**
     * 사용자 입찰 중인 경매 목록
     */
    fun getMyBiddingAuctions(
        userId: Long
    ): Map<String, Any> {
        // 사용자의 활성 입찰 조회
        val activeBids = bidJpaRepository.findByUserIdAndStatusOrderByBidTimeDesc(
            userId = userId,
            status = com.ch.auction.auction.domain.BidStatus.ACTIVE,
            pageable = PageRequest.of(0, 100)
        )
        
        if (activeBids.isEmpty()) {
            return mapOf(
                "totalLockedPoint" to 0L,
                "biddingCount" to 0,
                "auctions" to emptyList<Any>()
            )
        }
        
        val latestBidsByAuction = activeBids
            .groupBy { it.auctionId }
            .mapValues { (_, bids) -> bids.maxByOrNull { it.bidTime }!! }
        
        val auctionIds = latestBidsByAuction.keys.toList()
        val auctions = auctionJpaRepository.findAllById(auctionIds)
        val auctionMap = auctions.associateBy { it.id!! }
        
        val biddingAuctions = latestBidsByAuction.mapNotNull { (auctionId, bid) ->
            val auction = auctionMap[auctionId] ?: return@mapNotNull null
            val redisInfo = auctionRepository.getAuctionRedisInfo(auctionId)
            
            val isHighestBidder = redisInfo?.lastBidderId == userId
            val currentPrice = redisInfo?.currentPrice ?: auction.currentPrice
            
            val now = LocalDateTime.now()
            val bidAge = Duration.between(bid.bidTime, now)
            val timeUntilEnd = Duration.between(now, auction.endTime)
            
            val canCancel = auction.status == AuctionStatus.ONGOING && 
                            timeUntilEnd.toMinutes() >= 10
            
            mapOf(
                "auctionId" to auction.id!!,
                "auctionTitle" to auction.title,
                "auctionStatus" to auction.status.name,
                "currentPrice" to currentPrice,
                "myBidAmount" to bid.amount,
                "myBidStatus" to bid.status.name,
                "myBidTime" to bid.bidTime.toString(),
                "isHighestBidder" to isHighestBidder,
                "lockedPoint" to bid.amount,
                "endTime" to auction.endTime.toString(),
                "canCancel" to canCancel,
                "bidAge" to bidAge.toMinutes()
            )
        }
        
        // locked 포인트 계산 (경매별 최신 입찰 금액만 합산)
        val totalLockedPoint = latestBidsByAuction.values.sumOf { it.amount }
        
        return mapOf(
            "totalLockedPoint" to totalLockedPoint,
            "biddingCount" to activeBids.size,
            "auctions" to biddingAuctions
        )
    }
}
