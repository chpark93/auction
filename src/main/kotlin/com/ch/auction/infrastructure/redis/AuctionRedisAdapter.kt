package com.ch.auction.infrastructure.redis

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.domain.event.BidAcceptedEvent
import com.ch.auction.domain.repository.AuctionRepository
import com.ch.auction.domain.repository.BidResult
import com.ch.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.interfaces.common.ErrorCode
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.ZoneId
import java.util.*

@Component
class AuctionRedisAdapter(
    private val redisTemplate: StringRedisTemplate,
    private val eventPublisher: ApplicationEventPublisher,
    private val auctionJpaRepository: AuctionJpaRepository
) : AuctionRepository {

    private val bidLuaScript: RedisScript<Long> = RedisScript.of(
        ClassPathResource("scripts/bid_script.lua"),
        Long::class.java
    )

    override fun tryBid(
        auctionId: Long,
        userId: Long,
        amount: BigDecimal,
        requestTime: Long
    ): BidResult {
        val key = "auction:$auctionId"

        /**
         * ARGV[1]: amount
         * ARGV[2]: userId
         * ARGV[3]: requestTime
         */
        val result = redisTemplate.execute(
            bidLuaScript,
            Collections.singletonList(key),
            amount.toPlainString(),
            userId.toString(),
            requestTime.toString()
        )

        return when (result) {
            1L -> {
                eventPublisher.publishEvent(
                    BidAcceptedEvent(
                        auctionId = auctionId,
                        userId = userId,
                        amount = amount.toLong(),
                        bidTime = java.time.Instant.ofEpochMilli(requestTime)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                    )
                )
                BidResult.Success(amount)
            }
            0L -> BidResult.PriceTooLow
            -1L -> BidResult.AuctionNotFound
            -2L -> BidResult.AuctionEnded
            else -> throw BusinessException(ErrorCode.UNEXPECTED_STATE_LUA_SCRIPT)
        }
    }

    @Transactional(readOnly = true)
    override fun loadAuctionToRedis(
        auctionId: Long
    ) {
        val auction = auctionJpaRepository.findById(auctionId).orElseThrow {
            throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)
        }

        val key = "auction:$auctionId"
        val endTimeMillis = auction.endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val map = mapOf(
            "currentPrice" to auction.currentPrice.toString(),
            "endTime" to endTimeMillis.toString()
        )

        redisTemplate.opsForHash<String, String>().putAll(key, map)
    }
}
