package com.ch.auction.auction.application.service

import com.ch.auction.auction.domain.AuctionRepository
import com.ch.auction.auction.domain.BidResult
import com.ch.auction.auction.infrastructure.client.user.UserClient
import com.ch.auction.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.auction.infrastructure.redis.SellerInfoCacheRepository
import com.ch.auction.auction.infrastructure.redis.UserStatusCacheRepository
import com.ch.auction.auction.interfaces.api.dto.AuctionResponse
import com.ch.auction.common.ErrorCode
import com.ch.auction.common.enums.UserStatus
import com.ch.auction.exception.BusinessException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AuctionService(
    private val auctionRepository: AuctionRepository,
    private val auctionJpaRepository: AuctionJpaRepository,
    private val sellerInfoCacheRepository: SellerInfoCacheRepository,
    private val userStatusCacheRepository: UserStatusCacheRepository,
    private val userClient: UserClient
) {
    /**
     * 입찰
     */
    fun placeBid(
        auctionId: Long,
        userId: Long,
        amount: Long
    ): Long {
        // 유저 상태 검증 (Redis Cache 활용)
        verifyUserStatus(userId)

        val requestTime = Instant.now().toEpochMilli()

        val result = auctionRepository.tryBid(
            auctionId = auctionId,
            userId = userId,
            amount = amount,
            requestTime = requestTime
        )

        return when (result) {
            is BidResult.Success -> result.currentPrice
            is BidResult.PriceTooLow -> throw BusinessException(ErrorCode.PRICE_TOO_LOW)
            is BidResult.AuctionEnded -> throw BusinessException(ErrorCode.AUCTION_ENDED)
            is BidResult.AuctionNotFound -> throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)
            is BidResult.SelfBidding -> throw BusinessException(ErrorCode.SELF_BIDDING_NOT_ALLOWED)
            is BidResult.NotEnoughPoint -> throw BusinessException(ErrorCode.NOT_ENOUGH_POINT)
        }
    }

    fun getAuctionCurrentPrice(
        auctionId: Long
    ): Long {
        val info = auctionRepository.getAuctionRedisInfo(
            auctionId = auctionId
        ) ?: throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)

        return info.currentPrice
    }

    fun getAuction(
        auctionId: Long
    ): AuctionResponse {
        val auction = auctionJpaRepository.findByIdOrNull(auctionId)
            ?: throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)

        val sellerName = getSellerName(
            userId = auction.sellerId
        )

        return AuctionResponse.from(
            auction = auction,
            sellerName = sellerName
        )
    }

    private fun getSellerName(
        userId: Long
    ): String {
        // Redis Cache 조회
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
        // Redis Cache 조회
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
}
