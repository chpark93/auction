package com.ch.auction.auction.domain

import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class AuctionTest {

    @Test
    fun create() {
        // given
        val title = "Test Auction"
        val startPrice = 10000L
        val sellerId = 100L
        val startTime = LocalDateTime.now().plusDays(1)
        val endTime = LocalDateTime.now().plusDays(2)

        // when
        val auction = Auction.create(
            title = title,
            startPrice = startPrice,
            startTime = startTime,
            endTime = endTime,
            sellerId = sellerId
        )

        // then
        assertEquals(AuctionStatus.PENDING, auction.status)
        assertEquals(sellerId, auction.sellerId)
        assertEquals(title, auction.title)
        assertEquals(startPrice, auction.startPrice)
        assertEquals(startPrice, auction.currentPrice)
    }

    @Test
    fun start_auction() {
        // given
        val auction = Auction.create(
            title = "Test",
            startPrice = 10000L,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusHours(1),
            sellerId = 100L
        )
        // PENDING -> APPROVED로 먼저 변경
        auction.approve()

        // when
        auction.startAuction()

        // then
        assertEquals(AuctionStatus.ONGOING, auction.status)
    }

    @Test
    fun close_auction_ongoing_to_ended() {
        // given
        val auction = Auction.create(
            title = "Test",
            startPrice = 10000L,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusHours(1),
            sellerId = 100L
        )
        auction.approve()
        auction.startAuction()

        // when
        auction.closeAuction()

        // then
        assertEquals(AuctionStatus.ENDED, auction.status)
    }

    @Test
    fun close_auction_not_ongoing() {
        // given
        val auction = Auction.create(
            title = "Test",
            startPrice = 10000L,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusHours(1),
            sellerId = 100L
        )
        // PENDING

        // when & then
        val exception = assertThrows<BusinessException> {
            auction.closeAuction()
        }
        assertEquals(ErrorCode.AUCTION_NOT_ONGOING, exception.errorCode)
    }

    @Test
    fun close_auction_ended_to_ended() {
        // given
        val auction = Auction.create(
            title = "Test",
            startPrice = 10000L,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusHours(1),
            sellerId = 100L
        )

        auction.approve()
        auction.startAuction()
        auction.closeAuction() // 첫 번째 종료

        // when & then
        val exception = assertThrows<BusinessException> {
            auction.closeAuction() // 두 번째 종료 시도
        }
        assertEquals(ErrorCode.AUCTION_NOT_ONGOING, exception.errorCode)
    }

    @Test
    fun complete_auction_ended_to_completed() {
        // given
        val auction = Auction.create(
            title = "Test",
            startPrice = 10000L,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusHours(1),
            sellerId = 100L
        )

        auction.approve()
        auction.startAuction()
        auction.closeAuction()

        // when
        auction.completeAuction()

        // then
        assertEquals(AuctionStatus.COMPLETED, auction.status)
    }

    @Test
    fun fail_auction_ended_to_failed() {
        // given
        val auction = Auction.create(
            title = "Test",
            startPrice = 10000L,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusHours(1),
            sellerId = 100L
        )

        auction.approve()
        auction.startAuction()
        auction.closeAuction()

        // when
        auction.failAuction()

        // then
        assertEquals(AuctionStatus.FAILED, auction.status)
    }
}

