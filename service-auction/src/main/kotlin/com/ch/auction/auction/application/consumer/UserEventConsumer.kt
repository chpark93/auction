package com.ch.auction.auction.application.consumer

import com.ch.auction.auction.infrastructure.redis.SellerInfoCacheRepository
import com.ch.auction.auction.infrastructure.redis.UserStatusCacheRepository
import com.ch.auction.common.event.UserUpdatedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class UserEventConsumer(
    private val sellerInfoCacheRepository: SellerInfoCacheRepository,
    private val userStatusCacheRepository: UserStatusCacheRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["user-update"],
        groupId = "auction-group"
    )
    fun handleUserUpdated(
        event: UserUpdatedEvent
    ) {
        logger.info("Received UserUpdatedEvent: userId={}, nickname={}", event.userId, event.nickname)
        
        // 캐시 갱신
        sellerInfoCacheRepository.saveSellerNickName(
            userId = event.userId,
            nickname = event.nickname
        )

        userStatusCacheRepository.deleteUserStatus(
            userId = event.userId
        )
    }
}
