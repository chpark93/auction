package com.ch.auction.infrastructure.redis

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.domain.event.BidSuccessEvent
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

    private val bidLuaScript: RedisScript<String> = RedisScript.of(
        ClassPathResource("scripts/bid_script.lua"),
        String::class.java
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
            "0" -> BidResult.PriceTooLow
            "-1" -> BidResult.AuctionNotFound
            "-2" -> BidResult.AuctionEnded
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
                        bidTime = java.time.Instant.ofEpochMilli(timestamp)
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
            "bidSequence" to "0" // 시퀀스 초기화
        )

        // 기존 데이터가 있다면 삭제 후 적재
        redisTemplate.delete(key)
        redisTemplate.opsForHash<String, String>().putAll(key, map)
    }
}
