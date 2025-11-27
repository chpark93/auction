package com.ch.auction.infrastructure.redis

import com.ch.auction.domain.event.BidAcceptedEvent
import com.ch.auction.domain.repository.BidResult
import com.ch.auction.infrastructure.persistence.AuctionJpaRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class AuctionRedisAdapterTest {

    @Mock
    private lateinit var redisTemplate: StringRedisTemplate

    @Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    @Mock
    private lateinit var auctionJpaRepository: AuctionJpaRepository

    @InjectMocks
    private lateinit var auctionRedisAdapter: AuctionRedisAdapter

    @Test
    fun bid_success_event_publish_success() {
        // given
        val auctionId = 1L
        val userId = 100L
        val amount = BigDecimal("5000")
        val requestTime = System.currentTimeMillis()

        `when`(redisTemplate.execute(
            any<RedisScript<Long>>(),
            anyList(),
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(1L)

        // when
        val result = auctionRedisAdapter.tryBid(
            auctionId = auctionId,
            userId = userId,
            amount = amount,
            requestTime = requestTime
        )

        // then
        assertTrue(result is BidResult.Success)
        verify(eventPublisher).publishEvent(any(BidAcceptedEvent::class.java))
    }
}
