package com.ch.auction.auction.application.service

import com.ch.auction.auction.domain.AuctionRepository
import com.ch.auction.auction.domain.BidResult
import com.ch.auction.auction.infrastructure.client.user.UserClient
import com.ch.auction.auction.infrastructure.client.user.dtos.UserClientDtos
import com.ch.auction.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.auction.infrastructure.redis.SellerInfoCacheRepository
import com.ch.auction.auction.infrastructure.redis.UserStatusCacheRepository
import com.ch.auction.common.ApiResponse
import com.ch.auction.common.ErrorCode
import com.ch.auction.common.enums.UserStatus
import com.ch.auction.exception.BusinessException
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AuctionServiceTest {

    private val auctionRepository: AuctionRepository = mockk()
    private val auctionJpaRepository: AuctionJpaRepository = mockk()
    private val sellerInfoCacheRepository: SellerInfoCacheRepository = mockk()
    private val userStatusCacheRepository: UserStatusCacheRepository = mockk()
    private val userClient: UserClient = mockk()

    private val auctionService = AuctionService(
        auctionRepository = auctionRepository,
        auctionJpaRepository = auctionJpaRepository,
        sellerInfoCacheRepository = sellerInfoCacheRepository,
        userStatusCacheRepository = userStatusCacheRepository,
        userClient = userClient
    )

    @Test
    fun place_bid_cached_user_status_active() {
        // given
        val auctionId = 1L
        val userId = 100L
        val amount = 10000L
        val requestTime = 1234567890L

        every {
            userStatusCacheRepository.getUserStatus(
                userId = userId
            )
        } returns "ACTIVE"
        
        every {
            auctionRepository.tryBid(
                auctionId = auctionId,
                userId = userId,
                amount = amount,
                requestTime = any()
            )
        } returns BidResult.Success(amount)

        // when
        val result = auctionService.placeBid(
            auctionId = auctionId,
            userId = userId,
            amount = amount
        )

        // then
        assertEquals(amount, result)
        verify(exactly = 0) {
            userClient.getUserInfo(
                userId = any()
            )
        }
        verify(exactly = 1) {
            auctionRepository.tryBid(
                auctionId = auctionId,
                userId = userId,
                amount = amount,
                requestTime = any()
            )
        }
    }

    @Test
    fun place_bid_none_cached_user_status_active() {
        // given
        val auctionId = 1L
        val userId = 100L
        val amount = 10000L

        // cache miss
        every {
            userStatusCacheRepository.getUserStatus(
                userId = userId
            )
        } returns null
        
        // feign call
        val userResponse = UserClientDtos.UserResponse(
            id = userId,
            email = "test@test.com",
            nickname = "tester",
            name = "Test",
            role = "USER",
            status = UserStatus.ACTIVE
        )

        every {
            userClient.getUserInfo(
                userId = userId
            )
        } returns ApiResponse.ok(userResponse)

        // cache save
        every {
            userStatusCacheRepository.saveUserStatus(
                userId = userId,
                status = "ACTIVE"
            )
        } just Runs

        every { 
            auctionRepository.tryBid(
                auctionId = auctionId,
                userId = userId,
                amount = amount,
                requestTime = any()
            )
        } returns BidResult.Success(amount)

        // when
        val result = auctionService.placeBid(
            auctionId = auctionId,
            userId = userId,
            amount = amount
        )

        // then
        assertEquals(amount, result)
        verify(exactly = 1) {
            userClient.getUserInfo(
                userId = userId
            )
        }
        verify(exactly = 1) {
            userStatusCacheRepository.saveUserStatus(
                userId = userId,
                status = "ACTIVE"
            )
        }
        verify(exactly = 1) {
            auctionRepository.tryBid(
                auctionId = auctionId,
                userId = userId,
                amount = amount,
                requestTime = any()
            )
        }
    }

    @Test
    fun place_bid_cached_user_status_banned() {
        // given
        val userId = 100L
        every {
            userStatusCacheRepository.getUserStatus(
                userId = userId
            )
        } returns "BANNED"

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            auctionService.placeBid(
                auctionId = 1L,
                userId = userId,
                amount = 10000L
            )
        }

        assertEquals(ErrorCode.USER_NOT_ACTIVE, exception.errorCode)
        verify(exactly = 0) {
            userClient.getUserInfo(
                userId = any()
            )
        }
        verify(exactly = 0) {
            auctionRepository.tryBid(
                auctionId = any(),
                userId = any(),
                amount = any(),
                requestTime = any()
            )
        }
    }

    @Test
    fun place_bid_none_cached_user_status_banned() {
        // given
        val userId = 100L
        every {
            userStatusCacheRepository.getUserStatus(
                userId = userId
            )
        } returns null

        val userResponse = UserClientDtos.UserResponse(
            id = userId,
            email = "test@test.com",
            nickname = "tester",
            name = "Test",
            role = "USER",
            status = UserStatus.BANNED
        )
        every {
            userClient.getUserInfo(
                userId = userId
            )
        } returns ApiResponse.ok(userResponse)
        
        // 비활성 상태 캐싱
        every {
            userStatusCacheRepository.saveUserStatus(
                userId = userId,
                status = "BANNED"
            )
        } just Runs

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            auctionService.placeBid(
                auctionId = 1L,
                userId = userId,
                amount = 10000L
            )
        }

        assertEquals(ErrorCode.USER_NOT_ACTIVE, exception.errorCode)
        verify(exactly = 1) {
            userStatusCacheRepository.saveUserStatus(
                userId = userId,
                status = "BANNED"
            )
        }
        verify(exactly = 0) {
            auctionRepository.tryBid(
                auctionId = any(),
                userId = any(),
                amount = any(),
                requestTime = any()
            )
        }
    }

    @Test
    fun place_bid_fail_feign_call_user_status() {
        // given
        val userId = 100L
        every {
            userStatusCacheRepository.getUserStatus(
                userId = userId
            )
        } returns null
        every {
            userClient.getUserInfo(
                userId = userId
            )
        } throws RuntimeException("Circuit Breaker Open")

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            auctionService.placeBid(
                auctionId = 1L,
                userId = userId,
                amount = 10000L
            )
        }

        assertEquals(ErrorCode.USER_SERVICE_UNAVAILABLE, exception.errorCode)
        verify(exactly = 0) {
            auctionRepository.tryBid(
                auctionId = any(),
                userId = any(),
                amount = any(),
                requestTime = any()
            )
        }
    }
}

