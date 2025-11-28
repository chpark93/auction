package com.ch.auction.infrastructure.redis

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.domain.AuctionLuaResult
import com.ch.auction.domain.event.BidSuccessEvent
import com.ch.auction.domain.repository.AuctionRedisInfo
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
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.*

@Component
class AuctionRedisAdapter(
    private val redisTemplate: StringRedisTemplate,
    private val eventPublisher: ApplicationEventPublisher,
    private val auctionJpaRepository: AuctionJpaRepository
) : AuctionRepository {

    private val bidLuaScript: RedisScript<String> = RedisScript.of(
        ClassPathResource("scripts/bid_script.lua"),
        String::class.java
    )

    override fun tryBid(
        auctionId: Long,
        userId: Long,
        amount: BigDecimal,
        requestTime: Long,
        maxLimit: BigDecimal
    ): BidResult {
        val key = "auction:$auctionId"

        /**
         * ARGV[1]: amount
         * ARGV[2]: userId
         * ARGV[3]: requestTime
         * ARGV[4]: minIncrement
         * ARGV[5]: maxLimit
         */
        val result = redisTemplate.execute(
            bidLuaScript,
            Collections.singletonList(key),
            amount.toPlainString(),
            userId.toString(),
            requestTime.toString(),
            "1000", // minIncrement
            maxLimit.toPlainString()
        )

        return when (result) {
            AuctionLuaResult.PRICE_TOO_LOW.code -> BidResult.PriceTooLow
            AuctionLuaResult.AUCTION_NOT_FOUND.code -> BidResult.AuctionNotFound
            AuctionLuaResult.AUCTION_ENDED.code -> BidResult.AuctionEnded
            AuctionLuaResult.SELF_BIDDING.code -> BidResult.SelfBidding
            AuctionLuaResult.NOT_ENOUGH_POINT.code -> BidResult.NotEnoughPoint
            AuctionLuaResult.OUTBID.code -> BidResult.Outbidded
            else -> {
                val parts = result.split(":")
                if (parts.size != 2) {
                    throw BusinessException(ErrorCode.UNEXPECTED_STATE_LUA_SCRIPT)
                }

                val timestamp = parts[0].toLong()
                val sequence = parts[1].toLong()

                eventPublisher.publishEvent(
                    BidSuccessEvent(
                        auctionId = auctionId,
                        userId = userId,
                        amount = amount,
                        bidTime = Instant.ofEpochMilli(timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime(),
                        sequence = sequence
                    )
                )

                BidResult.Success(
                    newPrice = amount
                )
            }
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
            "endTime" to endTimeMillis.toString(),
            "bidSequence" to "0",
            "sellerId" to auction.sellerId.toString()
        )

        // 기존 데이터가 있다면 삭제 후 적재
        redisTemplate.delete(key)
        redisTemplate.opsForHash<String, String>().putAll(key, map)
    }

    override fun getAuctionRedisInfo(
        auctionId: Long
    ): AuctionRedisInfo? {
        val key = "auction:$auctionId"
        val entries = redisTemplate.opsForHash<String, String>().entries(key)
        if (entries.isEmpty()) return null

        val currentPrice = entries["currentPrice"]?.toLongOrNull() ?: 0L
        val lastBidderId = entries["lastBidderId"]?.toLongOrNull()

        return AuctionRedisInfo(
            currentPrice = currentPrice,
            lastBidderId = lastBidderId
        )
    }

    override fun deleteAuctionRedisInfo(
        auctionId: Long
    ) {
        val key = "auction:$auctionId"
        redisTemplate.delete(key)
    }

    override fun expireAuctionRedisInfo(
        auctionId: Long,
        seconds: Long
    ) {
        val key = "auction:$auctionId"
        redisTemplate.expire(key, Duration.ofSeconds(seconds))
    }
}
