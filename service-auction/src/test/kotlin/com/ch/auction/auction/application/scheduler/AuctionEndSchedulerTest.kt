package com.ch.auction.auction.application.scheduler

import com.ch.auction.auction.application.dto.AuctionRedisDtos
import com.ch.auction.auction.domain.Auction
import com.ch.auction.auction.domain.AuctionRepository
import com.ch.auction.auction.domain.AuctionStatus
import com.ch.auction.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.auction.infrastructure.sse.SseEmitterManager
import com.ch.auction.common.event.AuctionEndedEvent
import com.ch.auction.common.event.NotificationEvent
import com.ch.auction.common.event.NotificationType
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDateTime

class AuctionEndSchedulerTest {

    private val auctionJpaRepository: AuctionJpaRepository = mockk()
    private val auctionRepository: AuctionRepository = mockk()
    private val sseEmitterManager: SseEmitterManager = mockk()
    private val kafkaTemplate: KafkaTemplate<String, Any> = mockk()

    private lateinit var auctionEndScheduler: AuctionEndScheduler

    @BeforeEach
    fun setUp() {
        auctionEndScheduler = AuctionEndScheduler(
            auctionJpaRepository = auctionJpaRepository,
            auctionRepository = auctionRepository,
            sseEmitterManager = sseEmitterManager,
            kafkaTemplate = kafkaTemplate
        )
    }

    @Test
    fun auction_bid_completed_publish_event() {
        // given
        val auctionId = 1L
        val sellerId = 100L
        val winnerId = 200L
        val finalPrice = 50000L

        val auction = Auction.create(
            productId = 1L,
            title = "Test Auction",
            thumbnailUrl = null,
            startPrice = 10000L,
            startTime = LocalDateTime.now().minusHours(2),
            endTime = LocalDateTime.now().minusMinutes(1),
            sellerId = sellerId
        )

        val idField = Auction::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(auction, auctionId)

        auction.approve()
        auction.startAuction()

        val redisInfo = AuctionRedisDtos.AuctionRedisInfo(
            currentPrice = finalPrice,
            lastBidderId = winnerId
        )

        every { 
            auctionJpaRepository.findAllByStatusAndEndTimeLessThanEqual(
                AuctionStatus.ONGOING,
                any()
            )
        } returns listOf(auction)

        every {
            auctionRepository.getAuctionRedisInfo(
                auctionId = auctionId
            )
        } returns redisInfo
        every {
            kafkaTemplate.send(any<String>(), any<Any>())
        } returns mockk(relaxed = true)
        every {
            sseEmitterManager.sendToAuction(any(), any(), any())
        } just Runs
        every {
            auctionRepository.expireAuctionRedisInfo(
                auctionId = auctionId,
                seconds = any()
            )
        } just Runs

        // when
        auctionEndScheduler.endScheduledAuctions()

        // then
        assertEquals(AuctionStatus.COMPLETED, auction.status)
        
        // AuctionEndedEvent 발행 확인
        verify(exactly = 1) {
            kafkaTemplate.send(
                "auction-ended",
                match<AuctionEndedEvent> {
                    it.auctionId == auctionId &&
                    it.sellerId == sellerId &&
                    it.winnerId == winnerId &&
                    it.finalPrice == finalPrice
                }
            )
        }

        // NotificationEvent 발행 확인 (낙찰자용)
        verify(exactly = 1) {
            kafkaTemplate.send(
                "notification-send",
                match<NotificationEvent> {
                    it.userId == winnerId &&
                    it.type == NotificationType.BID_SUCCESS
                }
            )
        }

        // NotificationEvent 발행 확인 (판매자용)
        verify(exactly = 1) {
            kafkaTemplate.send(
                "notification-send",
                match<NotificationEvent> {
                    it.userId == sellerId &&
                    it.type == NotificationType.AUCTION_ENDED
                }
            )
        }
    }

    @Test
    fun auction_bid_failed_publish_event() {
        // given
        val auctionId = 1L
        val sellerId = 100L

        val auction = Auction.create(
            productId = 1L,
            title = "Test Auction",
            thumbnailUrl = null,
            startPrice = 10000L,
            startTime = LocalDateTime.now().minusHours(2),
            endTime = LocalDateTime.now().minusMinutes(1),
            sellerId = sellerId
        )

        val idField = Auction::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(auction, auctionId)

        auction.approve()
        auction.startAuction()

        val redisInfo = AuctionRedisDtos.AuctionRedisInfo(
            currentPrice = 10000L,
            lastBidderId = null // 낙찰자 없음
        )

        every { 
            auctionJpaRepository.findAllByStatusAndEndTimeLessThanEqual(
                status = AuctionStatus.ONGOING,
                now = any()
            )
        } returns listOf(auction)

        every {
            auctionRepository.getAuctionRedisInfo(
                auctionId = auctionId
            )
        } returns redisInfo
        every {
            kafkaTemplate.send(any<String>(), any<Any>())
        } returns mockk(relaxed = true)
        every {
            sseEmitterManager.sendToAuction(any(), any(), any())
        } just Runs
        every {
            auctionRepository.expireAuctionRedisInfo(
                auctionId = auctionId,
                seconds = any()
            )
        } just Runs

        // when
        auctionEndScheduler.endScheduledAuctions()

        // then
        assertEquals(AuctionStatus.FAILED, auction.status)
        
        // AuctionEndedEvent 발행 확인 (winnerId가 null)
        verify(exactly = 1) {
            kafkaTemplate.send(
                "auction-ended",
                match<AuctionEndedEvent> {
                    it.auctionId == auctionId &&
                    it.sellerId == sellerId &&
                    it.winnerId == null &&
                    it.finalPrice == 0L
                }
            )
        }

        // NotificationEvent 발행 확인 (판매자에게 유찰 알림)
        verify(exactly = 1) {
            kafkaTemplate.send(
                "notification-send",
                match<NotificationEvent> {
                    it.userId == sellerId &&
                    it.type == NotificationType.AUCTION_FAILED
                }
            )
        }
    }
}

