package com.ch.auction.auction.infrastructure.redis

import com.ch.auction.auction.application.dto.AuctionRedisDtos
import com.ch.auction.auction.domain.AuctionLuaResult
import com.ch.auction.auction.domain.AuctionRepository
import com.ch.auction.auction.domain.BidResult
import com.ch.auction.auction.domain.event.BidSuccessEvent
import com.ch.auction.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
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

    companion object {
        private const val AUCTION_LUA_PREFIX = "auction"
    }

    override fun tryBid(
        auctionId: Long,
        userId: Long,
        amount: Long,
        requestTime: Long
    ): BidResult {
        val key = "$AUCTION_LUA_PREFIX:$auctionId"

        val result = executeLuaScript(
            key = key,
            userId = userId,
            amount = amount,
            requestTime = requestTime
        )

        return handleLuaResult(
            result = result,
            auctionId = auctionId,
            userId = userId,
            amount = amount
        )
    }

    private fun executeLuaScript(
        key: String,
        userId: Long,
        amount: Long,
        requestTime: Long
    ): String {
        return redisTemplate.execute(
            bidLuaScript,
            Collections.singletonList(key),
            amount.toString(),
            userId.toString(),
            requestTime.toString()
        )
    }

    private fun handleLuaResult(
        result: String,
        auctionId: Long,
        userId: Long,
        amount: Long
    ): BidResult {
        when (result) {
            AuctionLuaResult.PRICE_TOO_LOW.code -> return BidResult.PriceTooLow
            AuctionLuaResult.AUCTION_NOT_FOUND.code -> return BidResult.AuctionNotFound
            AuctionLuaResult.AUCTION_ENDED.code -> return BidResult.AuctionEnded
            AuctionLuaResult.SELF_BIDDING.code -> return BidResult.SelfBidding
            AuctionLuaResult.NOT_ENOUGH_POINT.code -> return BidResult.NotEnoughPoint
        }

        processSuccess(
            result = result,
            auctionId = auctionId,
            userId = userId,
            amount = amount
        )

        return BidResult.Success(
            currentPrice = amount
        )
    }

    private fun processSuccess(
        result: String,
        auctionId: Long,
        userId: Long,
        amount: Long
    ) {
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
    }

    @Transactional(readOnly = true)
    override fun loadAuctionToRedis(
        auctionId: Long
    ) {
        val auction = auctionJpaRepository.findById(auctionId).orElseThrow {
            throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)
        }

        val key = "$AUCTION_LUA_PREFIX:$auctionId"
        val endTimeMillis = auction.endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val map = mapOf(
            "currentPrice" to auction.currentPrice.toString(),
            "endTime" to endTimeMillis.toString(),
            "bidSequence" to "0",
            "sellerId" to auction.sellerId.toString()
        )

        // 기존 데이터가 있다면 삭제 후 삽입
        redisTemplate.delete(key)
        redisTemplate.opsForHash<String, String>().putAll(key, map)
    }

    override fun getAuctionRedisInfo(
        auctionId: Long
    ): AuctionRedisDtos.AuctionRedisInfo? {
        val key = "$AUCTION_LUA_PREFIX:$auctionId"
        val entries = redisTemplate.opsForHash<String, String>().entries(key)
        if (entries.isEmpty()) return null

        val currentPrice = entries["currentPrice"]?.toLongOrNull() ?: 0L
        val lastBidderId = entries["lastBidderId"]?.toLongOrNull()

        return AuctionRedisDtos.AuctionRedisInfo(
            currentPrice = currentPrice,
            lastBidderId = lastBidderId
        )
    }

    override fun deleteAuctionRedisInfo(
        auctionId: Long
    ) {
        val key = "$AUCTION_LUA_PREFIX:$auctionId"
        redisTemplate.delete(key)
    }

    override fun expireAuctionRedisInfo(
        auctionId: Long,
        seconds: Long
    ) {
        val key = "$AUCTION_LUA_PREFIX:$auctionId"
        redisTemplate.expire(key, Duration.ofSeconds(seconds))
    }
}
