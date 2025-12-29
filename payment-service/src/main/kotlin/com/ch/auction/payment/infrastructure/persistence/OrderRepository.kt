package com.ch.auction.payment.infrastructure.persistence

import com.ch.auction.payment.domain.DeliveryStatus
import com.ch.auction.payment.domain.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface OrderRepository : JpaRepository<Order, Long> {

    @Query("""
        SELECT 
            order
        FROM Order order
        JOIN order.delivery delivery
        WHERE delivery.status = :status 
        AND delivery.deliveredAt < :deliveredAt
    """)
    fun findAllByStatusAndDeliveredAtBefore(
        @Param("status") status: DeliveryStatus,
        @Param("deliveredAt") deliveredAt: LocalDateTime
    ): List<Order>

    fun findByAuctionId(
        auctionId: Long
    ): Optional<Order>
}
