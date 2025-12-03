package com.ch.auction.payment.application.consumer

import com.ch.auction.common.event.AuctionEndedEvent
import com.ch.auction.payment.application.service.OrderService
import com.ch.auction.payment.application.service.PaymentService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class AuctionEventConsumer(
    private val paymentService: PaymentService,
    private val orderService: OrderService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["auction-ended"],
        groupId = "payment-group"
    )
    fun handleAuctionEnded(
        event: AuctionEndedEvent
    ) {
        logger.info("Received AuctionEndedEvent: {}", event)

        if (event.winnerId != null) {
            try {
                // 정산
                paymentService.settleAuction(
                    userId = event.winnerId!!,
                    amount = event.finalPrice
                )

                // 주문 생성
                orderService.createOrder(
                    auctionId = event.auctionId,
                    buyerId = event.winnerId!!,
                    sellerId = event.sellerId,
                    payment = event.finalPrice
                )

                logger.info("Auction {} settlement and order creation completed.", event.auctionId)
            } catch (e: Exception) {
                logger.error("Failed to process AuctionEndedEvent for auction {}: {}", event.auctionId, e.message)
                // TODO: 재시도 로직 또는 롤백 처리 로직 필요
            }
        } else {
            logger.info("Auction {} ended without winner. No settlement required.", event.auctionId)
        }
    }
}
