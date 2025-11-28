package com.ch.auction.infrastructure.config

import com.ch.auction.domain.Auction
import com.ch.auction.domain.repository.AuctionRepository
import com.ch.auction.domain.repository.UserPointRepository
import com.ch.auction.infrastructure.persistence.AuctionJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TestDataInitializer(
    private val auctionJpaRepository: AuctionJpaRepository,
    private val auctionRepository: AuctionRepository,
    private val userPointRepository: UserPointRepository
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(
        args: ApplicationArguments?
    ) {
        if (auctionJpaRepository.count() == 0L) {
            logger.info("Initializing test data...")
            
            val auction = Auction(
                title = "Test Auction Item",
                startPrice = 1000L,
                startTime = LocalDateTime.now().minusMinutes(1),
                endTime = LocalDateTime.now().plusDays(1),
                sellerId = 9999L
            )

            auction.approve()
            auction.startAuction()
            
            val savedAuction = auctionJpaRepository.save(auction)
            auctionRepository.loadAuctionToRedis(
                auctionId = savedAuction.id!!
            )

            // 테스트 유저 (ID: 100) 포인트 충전
            userPointRepository.chargePoint(100L, 10000000L)
            logger.info("Charged 10,000,000 points to user 100")

            logger.info("Test Auction created: ID=${savedAuction.id}, Price=${savedAuction.startPrice}")
        }
    }
}
