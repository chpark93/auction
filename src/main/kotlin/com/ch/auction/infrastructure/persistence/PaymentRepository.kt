package com.ch.auction.infrastructure.persistence

import com.ch.auction.domain.Payment
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<Payment, Long> {

    fun existsByOrderId(
        orderId: String
    ): Boolean
}
