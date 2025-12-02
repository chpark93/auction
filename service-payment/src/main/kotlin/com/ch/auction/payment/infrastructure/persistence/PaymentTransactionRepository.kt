package com.ch.auction.payment.infrastructure.persistence

import com.ch.auction.payment.domain.PaymentTransaction
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentTransactionRepository : JpaRepository<PaymentTransaction, Long> {
    fun existsByTransactionKey(
        transactionKey: String
    ): Boolean
}