package com.ch.auction.infrastructure.config

import com.ch.auction.domain.Auction
import com.ch.auction.domain.repository.AuctionRepository
import com.ch.auction.infrastructure.persistence.AuctionJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TestDataInitializer(
    private val auctionJpaRepository: AuctionJpaRepository,
    private val auctionRepository: AuctionRepository
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
                endTime = LocalDateTime.now().plusDays(1)
            )

            auction.startAuction()
            
            val savedAuction = auctionJpaRepository.save(auction)
            auctionRepository.loadAuctionToRedis(
                auctionId = savedAuction.id!!
            )

            logger.info("Test Auction created: ID=${savedAuction.id}, Price=${savedAuction.startPrice}")
        }
    }
}

