package com.ch.auction.infrastructure.event

import com.ch.auction.domain.Bid
import com.ch.auction.domain.event.BidSuccessEvent
import com.ch.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.infrastructure.persistence.BidJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class BidPersistenceListener(
    private val bidJpaRepository: BidJpaRepository,
    private val auctionJpaRepository: AuctionJpaRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @EventListener
    @Transactional
    fun handleBidSuccess(event: BidSuccessEvent) {
        if (logger.isDebugEnabled) {
            logger.debug("Async processing in thread: {}", Thread.currentThread())
        }

        // Bid 저장 (이력)
        val bid = Bid(
            auctionId = event.auctionId,
            userId = event.userId,
            amount = event.amount.toLong(),
            bidTime = event.bidTime,
            sequence = event.sequence
        )

        bidJpaRepository.save(bid)

        // Auction 현재가 업데이트
        auctionJpaRepository.updateCurrentPriceIfHigher(
            id = event.auctionId,
            price = event.amount.toLong()
        )
    }
}
