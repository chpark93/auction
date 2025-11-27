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

    private val log = LoggerFactory.getLogger(javaClass)

    @Async
    @EventListener
    @Transactional
    fun handleBidSuccess(event: BidSuccessEvent) {
        // Virtual Thread 동작 확인 로그
        if (log.isDebugEnabled) {
            log.debug("Async processing in thread: {}", Thread.currentThread())
        }
        // 필요 시 info 레벨로 출력하여 확인 가능 (부하 테스트 시에는 로그 양이 많으므로 주의)
        // log.info("Async processing in thread: {}", Thread.currentThread())

        // Bid 저장 (이력)
        val bid = Bid(
            auctionId = event.auctionId,
            userId = event.userId,
            amount = event.amount.toLong(),
            bidTime = event.bidTime
        )

        bidJpaRepository.save(bid)

        // Auction 현재가 업데이트
        auctionJpaRepository.updateCurrentPriceIfHigher(
            id = event.auctionId,
            price = event.amount.toLong()
        )
    }
}
