package com.ch.auction.infrastructure.event

import com.ch.auction.domain.Bid
import com.ch.auction.domain.event.BidAcceptedEvent
import com.ch.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.infrastructure.persistence.BidJpaRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class BidEventListener(
    private val bidJpaRepository: BidJpaRepository,
    private val auctionJpaRepository: AuctionJpaRepository,
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {
    @Async
    @EventListener
    @Transactional
    fun handleBidAccepted(event: BidAcceptedEvent) {
        // Bid 엔티티 저장 (이력)
        val bid = Bid(
            auctionId = event.auctionId,
            userId = event.userId,
            amount = event.amount,
            bidTime = event.bidTime
        )

        bidJpaRepository.save(bid)

        // Auction 현재가 갱신
        // 현재가보다 높을 때만 업데이트
        auctionJpaRepository.updateCurrentPriceIfHigher(
            id = event.auctionId,
            price = event.amount
        )
        
        // 3. Redis message 발행
        val message = objectMapper.writeValueAsString(event)

        redisTemplate.convertAndSend("auction-updates", message)
    }
}

