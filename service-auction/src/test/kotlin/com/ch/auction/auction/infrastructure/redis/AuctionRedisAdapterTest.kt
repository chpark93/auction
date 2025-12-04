package com.ch.auction.auction.infrastructure.redis

import com.ch.auction.auction.domain.AuctionLuaResult
import com.ch.auction.auction.domain.BidResult
import com.ch.auction.auction.domain.event.BidSuccessEvent
import com.ch.auction.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript

class AuctionRedisAdapterTest {

    private val redisTemplate: StringRedisTemplate = mockk()
    private val eventPublisher: ApplicationEventPublisher = mockk()
    private val auctionJpaRepository: AuctionJpaRepository = mockk()

    private lateinit var auctionRedisAdapter: AuctionRedisAdapter

    @BeforeEach
    fun setUp() {
        auctionRedisAdapter = AuctionRedisAdapter(
            redisTemplate = redisTemplate,
            eventPublisher = eventPublisher,
            auctionJpaRepository = auctionJpaRepository
        )
    }

    @Test
    fun try_bid_return_success_and_publish_event() {
        // given
        val auctionId = 1L
        val userId = 100L
        val amount = 10000L
        val requestTime = 1234567890L
        val luaResult = "1234567890:1"

        every { 
            redisTemplate.execute<String>(
                any<RedisScript<String>>(),
                any<List<String>>(),
                any<String>(),
                any<String>(),
                any<String>()
            )
        } returns luaResult

        every {
            eventPublisher.publishEvent(any<BidSuccessEvent>())
        } just Runs

        // when
        val result = auctionRedisAdapter.tryBid(
            auctionId = auctionId,
            userId = userId,
            amount = amount,
            requestTime = requestTime
        )

        // then
        assertTrue(result is BidResult.Success)
        assertEquals(amount, (result as BidResult.Success).currentPrice)
        verify(exactly = 1) {
            eventPublisher.publishEvent(any<BidSuccessEvent>())
        }
    }

    @Test
    fun try_bid_low_price_bid() {
        // given
        val auctionId = 1L
        val userId = 100L
        val amount = 5000L
        val requestTime = 1234567890L

        every { 
            redisTemplate.execute<String>(
                any<RedisScript<String>>(),
                any<List<String>>(),
                any<String>(),
                any<String>(),
                any<String>()
            )
        } returns AuctionLuaResult.PRICE_TOO_LOW.code

        // when
        val result = auctionRedisAdapter.tryBid(
            auctionId = auctionId,
            userId = userId,
            amount = amount,
            requestTime = requestTime
        )

        // then
        assertTrue(result is BidResult.PriceTooLow)
        verify(exactly = 0) {
            eventPublisher.publishEvent(any())
        }
    }

    @Test
    fun try_bid_ended_auction() {
        // given
        val auctionId = 1L
        val userId = 100L
        val amount = 10000L
        val requestTime = 1234567890L

        every { 
            redisTemplate.execute<String>(
                any<RedisScript<String>>(),
                any<List<String>>(),
                any<String>(),
                any<String>(),
                any<String>()
            )
        } returns AuctionLuaResult.AUCTION_ENDED.code

        // when
        val result = auctionRedisAdapter.tryBid(
            auctionId = auctionId, 
            userId = userId, 
            amount = amount, 
            requestTime = requestTime
        )

        // then
        assertTrue(result is BidResult.AuctionEnded)
        verify(exactly = 0) {
            eventPublisher.publishEvent(any())
        }
    }

    @Test
    fun try_bid_not_found_auction() {
        // given
        val auctionId = 1L
        val userId = 100L
        val amount = 10000L
        val requestTime = 1234567890L

        every { 
            redisTemplate.execute<String>(
                any<RedisScript<String>>(),
                any<List<String>>(),
                any<String>(),
                any<String>(),
                any<String>()
            )
        } returns AuctionLuaResult.AUCTION_NOT_FOUND.code

        // when
        val result = auctionRedisAdapter.tryBid(
            auctionId = auctionId,
            userId = userId,
            amount = amount,
            requestTime = requestTime
        )

        // then
        assertTrue(result is BidResult.AuctionNotFound)
    }

    @Test
    fun try_bid_self_auction_bid() {
        // given
        val auctionId = 1L
        val userId = 100L
        val amount = 10000L
        val requestTime = 1234567890L

        every { 
            redisTemplate.execute<String>(
                any<RedisScript<String>>(),
                any<List<String>>(),
                any<String>(),
                any<String>(),
                any<String>()
            )
        } returns AuctionLuaResult.SELF_BIDDING.code

        // when
        val result = auctionRedisAdapter.tryBid(
            auctionId = auctionId,
            userId = userId,
            amount = amount,
            requestTime = requestTime
        )

        // then
        assertTrue(result is BidResult.SelfBidding)
    }

    @Test
    fun try_bid_not_enough_point() {
        // given
        val auctionId = 1L
        val userId = 100L
        val amount = 10000L
        val requestTime = 1234567890L

        every { 
            redisTemplate.execute<String>(
                any<RedisScript<String>>(),
                any<List<String>>(),
                any<String>(),
                any<String>(),
                any<String>()
            )
        } returns AuctionLuaResult.NOT_ENOUGH_POINT.code

        // when
        val result = auctionRedisAdapter.tryBid(
            auctionId = auctionId,
            userId = userId,
            amount = amount,
            requestTime = requestTime
        )

        // then
        assertTrue(result is BidResult.NotEnoughPoint)
    }

    @Test
    fun try_bid_invalid_lua_format() {
        // given
        val auctionId = 1L
        val userId = 100L
        val amount = 10000L
        val requestTime = 1234567890L
        val invalidResult = "invalid_format"

        every { 
            redisTemplate.execute<String>(
                any<RedisScript<String>>(),
                any<List<String>>(),
                any<String>(),
                any<String>(),
                any<String>()
            )
        } returns invalidResult

        // when & then
        val exception = assertThrows<BusinessException> {
            auctionRedisAdapter.tryBid(
                auctionId = auctionId,
                userId = userId,
                amount = amount,
                requestTime = requestTime
            )
        }
        assertEquals(ErrorCode.UNEXPECTED_STATE_LUA_SCRIPT, exception.errorCode)
    }
}
