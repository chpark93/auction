package com.ch.auction.application.scheduler

import com.ch.auction.application.service.SettlementService
import com.ch.auction.domain.DeliveryStatus
import com.ch.auction.infrastructure.persistence.OrderRepository
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 배송 완료 -> 구매 확정 처리
 * 실제 구현 시 주석 해제 및 필요한 의존성 주입 필요.
 */
@Component
class AutoConfirmScheduler(
    private val orderRepository: OrderRepository,
    private val settlementService: SettlementService
) {

    private val logger = LoggerFactory.getLogger(AutoConfirmScheduler::class.java)
    
    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(
        name = "AutoConfirmScheduler_confirmDeliveredOrders",
        lockAtLeastFor = "PT5M",
        lockAtMostFor = "PT30M"
    )
    fun confirmDeliveredOrders() {
        // 배송 완료(DELIVERED) 상태 -> 완료일로부터 7일이 지난 주문 조회
        val deliveredAt = LocalDateTime.now().minusDays(7)
        val orders = orderRepository.findAllByStatusAndDeliveredAtBefore(
            status = DeliveryStatus.DELIVERED,
            deliveredAt = deliveredAt
        )

        // 구매 확정 처리
        orders.forEach { order ->
            try {
                settlementService.confirmPurchase(
                    buyerId = order.buyerId,
                    orderId = order.id!!
                )
            } catch (e: Exception) {
                logger.error("Failed to auto-confirm order ${order.id}", e)
            }
        }
    }
}

