package com.ch.auction.auction.application.service

import com.ch.auction.auction.domain.Auction
import com.ch.auction.auction.domain.AuctionRepository
import com.ch.auction.auction.domain.AuctionStatus
import com.ch.auction.auction.domain.BidResult
import com.ch.auction.auction.infrastructure.client.product.ProductClient
import com.ch.auction.auction.infrastructure.client.search.SearchClient
import com.ch.auction.auction.infrastructure.client.user.UserClient
import com.ch.auction.auction.infrastructure.client.user.dtos.UserClientDtos
import com.ch.auction.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.auction.infrastructure.persistence.BidJpaRepository
import com.ch.auction.auction.infrastructure.redis.SellerInfoCacheRepository
import com.ch.auction.auction.infrastructure.redis.UserStatusCacheRepository
import com.ch.auction.common.ApiResponse
import com.ch.auction.common.ErrorCode
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.common.enums.UserStatus
import com.ch.auction.exception.BusinessException
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import io.mockk.junit5.MockKExtension
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockKExtension::class)
class AuctionServiceTest {

    private val auctionRepository: AuctionRepository = mockk()
    private val auctionJpaRepository: AuctionJpaRepository = mockk()
    private val bidJpaRepository: BidJpaRepository = mockk()
    private val sellerInfoCacheRepository: SellerInfoCacheRepository = mockk()
    private val userStatusCacheRepository: UserStatusCacheRepository = mockk()
    private val userClient: UserClient = mockk()
    private val productClient: ProductClient = mockk()
    private val bidCancellationService: BidCancellationService = mockk()
    private val searchClient: SearchClient = mockk()
    
    private lateinit var auctionService: AuctionService

    private val testAuction = Auction.create(
        productId = 1L,
        sellerId = 999L,
        title = "Test Auction",
        thumbnailUrl = null,
        startPrice = 1000L,
        startTime = LocalDateTime.now().minusDays(1),
        endTime = LocalDateTime.now().plusDays(1)
    ).apply {
        val idField = Auction::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(this, 1L)
        
        val statusField = Auction::class.java.getDeclaredField("status")
        statusField.isAccessible = true
        statusField.set(this, AuctionStatus.ONGOING)
    }

    @BeforeEach
    fun setUp() {
        auctionService = AuctionService(
            auctionRepository = auctionRepository,
            auctionJpaRepository = auctionJpaRepository,
            bidJpaRepository = bidJpaRepository,
            sellerInfoCacheRepository = sellerInfoCacheRepository,
            userStatusCacheRepository = userStatusCacheRepository,
            userClient = userClient,
            productClient = productClient,
            bidCancellationService = bidCancellationService,
            searchClient = searchClient
        )
    }

    @Test
    fun place_bid_cached_user_status_active() {
        // given
        val auctionId = 1L
        val userId = 100L
        val amount = 10000L

        every { auctionJpaRepository.findById(auctionId) } returns Optional.of(testAuction)
        every { auctionRepository.getAuctionRedisInfo(auctionId) } returns mockk()
        every { userStatusCacheRepository.getUserStatus(userId) } returns "ACTIVE"
        every { userClient.getUserPoint(any(), any()) } returns ApiResponse.ok(
            PointDTOs.PointResponse(
                userId = userId,
                totalPoint = 100000L,
                lockedPoint = 0L,
                availablePoint = 100000L
            )
        )
        every {
            auctionRepository.tryBid(
                auctionId = auctionId,
                userId = userId,
                amount = amount,
                requestTime = any(),
                userPoint = any()
            )
        } returns BidResult.Success(amount)
        every {
            userClient.holdPoint(
                userId = userId,
                request = any()
            )
        } returns ApiResponse.ok(Unit)

        // when
        val result = auctionService.placeBid(auctionId, userId, amount)

        // then
        assertEquals(amount, result)
        verify(exactly = 0) { userClient.getUserInfo(userId) }
        verify(exactly = 1) { auctionRepository.tryBid(any(), any(), any(), any(), any()) }
        verify(exactly = 1) { userClient.holdPoint(any(), any()) }
    }

    @Test
    fun place_bid_cached_user_status_banned() {
        // given
        val auctionId = 1L
        val userId = 100L
        val amount = 10000L

        every { auctionJpaRepository.findById(auctionId) } returns Optional.of(testAuction)
        every { auctionRepository.getAuctionRedisInfo(auctionId) } returns mockk()
        every { userStatusCacheRepository.getUserStatus(userId) } returns "BANNED"

        // when & then
        val exception = assertThrows<BusinessException> {
            auctionService.placeBid(auctionId, userId, amount)
        }

        assertEquals(ErrorCode.USER_NOT_ACTIVE, exception.errorCode)
        verify(exactly = 0) { auctionRepository.tryBid(any(), any(), any(), any(), any()) }
    }

    @Test
    fun place_bid_none_cached_user_status_active() {
        // given
        val auctionId = 1L
        val userId = 100L
        val amount = 10000L

        every { auctionJpaRepository.findById(auctionId) } returns Optional.of(testAuction)
        every { auctionRepository.getAuctionRedisInfo(auctionId) } returns mockk()
        every { userStatusCacheRepository.getUserStatus(userId) } returns null
        every { userClient.getUserInfo(userId) } returns ApiResponse.ok(
            UserClientDtos.UserResponse(
                id = userId,
                email = "test@test.com",
                nickname = "tester",
                name = "Test",
                role = "USER",
                status = UserStatus.ACTIVE
            )
        )
        every { userStatusCacheRepository.saveUserStatus(userId, "ACTIVE") } just Runs
        every { userClient.getUserPoint(any(), any()) } returns ApiResponse.ok(
            PointDTOs.PointResponse(
                userId = userId,
                totalPoint = 100000L,
                lockedPoint = 0L,
                availablePoint = 100000L
            )
        )
        every {
            auctionRepository.tryBid(
                auctionId = auctionId,
                userId = userId,
                amount = amount,
                requestTime = any(),
                userPoint = any()
            )
        } returns BidResult.Success(amount)
        every {
            userClient.holdPoint(
                userId = userId,
                request = any()
            )
        } returns ApiResponse.ok(Unit)

        // when
        val result = auctionService.placeBid(auctionId, userId, amount)

        // then
        assertEquals(amount, result)
        verify(exactly = 1) { userClient.getUserInfo(userId) }
        verify(exactly = 1) { auctionRepository.tryBid(any(), any(), any(), any(), any()) }
    }

    @Test
    fun place_bid_none_cached_user_status_banned() {
        // given
        val auctionId = 1L
        val userId = 100L
        val amount = 10000L

        every { auctionJpaRepository.findById(auctionId) } returns Optional.of(testAuction)
        every { auctionRepository.getAuctionRedisInfo(auctionId) } returns mockk()
        every { userStatusCacheRepository.getUserStatus(userId) } returns null
        every { userClient.getUserInfo(userId) } returns ApiResponse.ok(
            UserClientDtos.UserResponse(
                id = userId,
                email = "test@test.com",
                nickname = "tester",
                name = "Test",
                role = "USER",
                status = UserStatus.BANNED
            )
        )
        every { userStatusCacheRepository.saveUserStatus(userId, "BANNED") } just Runs

        // when & then
        val exception = assertThrows<BusinessException> {
            auctionService.placeBid(auctionId, userId, amount)
        }

        assertEquals(ErrorCode.USER_NOT_ACTIVE, exception.errorCode)
        verify(exactly = 0) { auctionRepository.tryBid(any(), any(), any(), any(), any()) }
    }

    @Test
    fun place_bid_fail_feign_call_user_status() {
        // given
        val auctionId = 1L
        val userId = 100L
        val amount = 10000L

        every { auctionJpaRepository.findById(auctionId) } returns Optional.of(testAuction)
        every { auctionRepository.getAuctionRedisInfo(auctionId) } returns mockk()
        every { userStatusCacheRepository.getUserStatus(userId) } returns null
        every { userClient.getUserInfo(userId) } throws RuntimeException("Feign call failed")

        // when & then
        val exception = assertThrows<BusinessException> {
            auctionService.placeBid(auctionId, userId, amount)
        }

        assertEquals(ErrorCode.USER_SERVICE_UNAVAILABLE, exception.errorCode)
        verify(exactly = 0) { auctionRepository.tryBid(any(), any(), any(), any(), any()) }
    }
}
