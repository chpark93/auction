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

        val resultCode = result?.toLongOrNull()
            ?: throw BusinessException(ErrorCode.UNEXPECTED_STATE_LUA_SCRIPT)

        return when {
            resultCode > 0 -> {
                // 성공: resultCode는 Redis 타임스탬프
                eventPublisher.publishEvent(
                    BidSuccessEvent(
                        auctionId = auctionId,
                        userId = userId,
                        amount = amount,
                        bidTime = java.time.Instant.ofEpochMilli(resultCode)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                    )
                )
                BidResult.Success(amount)
            }
            resultCode == 0L -> BidResult.PriceTooLow
            resultCode == -1L -> BidResult.AuctionNotFound
            resultCode == -2L -> BidResult.AuctionEnded
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

        // 기존 데이터가 있다면 삭제 후 적재 (초기화 보장)
        redisTemplate.delete(key)
        redisTemplate.opsForHash<String, String>().putAll(key, map)
    }
}
