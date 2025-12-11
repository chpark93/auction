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
import java.time.Instant

@Service
class AuctionService(
    private val auctionRepository: AuctionRepository,
    private val auctionJpaRepository: AuctionJpaRepository,
    private val bidJpaRepository: BidJpaRepository,
    private val sellerInfoCacheRepository: SellerInfoCacheRepository,
    private val userStatusCacheRepository: UserStatusCacheRepository,
    private val userClient: UserClient
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
        
        // 포인트 조회
        val userPoint = try {
            val response = userClient.getUserPoint(userId)
            response.data ?: 0L
        } catch (e: Exception) {
            logger.error("Failed to get user point", e)
            throw BusinessException(ErrorCode.USER_SERVICE_UNAVAILABLE)
        }

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
                    // 포인트 홀드 실패 시에도 입찰은 성공으로 처리 (비동기로 처리하는 것이 좋음)
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
        // DB에서 경매 확인
        val auction = auctionJpaRepository.findByIdOrNull(auctionId)
            ?: throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)
        
        // Redis 정보 조회 (없으면 로드)
        var info = auctionRepository.getAuctionRedisInfo(auctionId)
        if (info == null) {
            auctionRepository.loadAuctionToRedis(auctionId)
            info = auctionRepository.getAuctionRedisInfo(auctionId)
                ?: throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)
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
            val userEmail = usersMap[bid.userId]?.email ?: "None"

            BidHistoryResponse.from(
                bid = bid,
                userEmail = userEmail
            )
        }
    }
}
