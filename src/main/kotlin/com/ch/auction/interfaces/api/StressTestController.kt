package com.ch.auction.interfaces.api

import com.ch.auction.domain.repository.AuctionRepository
import com.ch.auction.domain.repository.BidResult
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant

@RestController
@RequestMapping("/api/test")
class StressTestController(
    private val auctionRepository: AuctionRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/bid")
    fun stressBid(
        @RequestParam auctionId: Long,
        @RequestParam userId: Long,
        @RequestParam amount: BigDecimal
    ): String {
        if (logger.isDebugEnabled) {
            logger.info("Handling request on thread: {}", Thread.currentThread())
        }

        val requestTime = Instant.now().toEpochMilli()
        
        return when (auctionRepository.tryBid(auctionId, userId, amount, requestTime)) {
            is BidResult.Success -> "OK"
            else -> "FAIL"
        }
    }
}
