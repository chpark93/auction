package com.ch.auction.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class AuctionTest {

    @Test
    fun bid_success_update_current_price() {
        // given
        val auction = createAuction(
            startPrice = 1000
        )

        // READY -> ONGOING
        auction.startAuction()

        val userId = 1L
        val amount = 2000L

        // when
        val bid = auction.placeBid(
            userId = userId,
            amount = amount
        )

        // then
        assertEquals(amount, auction.currentPrice)
        assertEquals(amount, bid.amount)
        assertEquals(userId, bid.userId)
    }

    @Test
    fun bid_lower_than_current_price_throws_exception() {
        // given
        val auction = createAuction(
            startPrice = 1000
        )

        auction.startAuction()
        
        // when & then
        assertThrows(IllegalArgumentException::class.java) {
            auction.placeBid(1L, 100L)
        }
    }

    @Test
    fun bid_when_auction_status_is_not_ongoing_throws_exception() {
        // given
        val auction = createAuction(startPrice = 1000)
        // startAuction() 호출 안 함 -> READY 상태

        // when & then
        assertThrows(IllegalStateException::class.java) {
            auction.placeBid(1L, 2000L)
        }
    }

    @Test
    fun bid_after_auction_end_time_throws_exception() {
        // given
        val auction = createAuction(endTime = LocalDateTime.now().minusMinutes(1))

        // ONGOING 상태지만 시간은 지남
        auction.startAuction()

        // when & then
        assertThrows(IllegalStateException::class.java) {
            auction.placeBid(1L, 2000L)
        }
    }

    private fun createAuction(
        startPrice: Long = 1000,
        startTime: LocalDateTime = LocalDateTime.now().minusHours(1),
        endTime: LocalDateTime = LocalDateTime.now().plusHours(1)
    ): Auction {
        val auction = Auction(
            title = "Test Auction",
            startPrice = startPrice,
            startTime = startTime,
            endTime = endTime
        )
        
        val idField = Auction::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(auction, 1L)
        
        return auction
    }
}

