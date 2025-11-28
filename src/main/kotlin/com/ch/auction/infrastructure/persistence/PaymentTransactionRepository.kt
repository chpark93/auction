package com.ch.auction.infrastructure.persistence

import com.ch.auction.domain.PaymentTransaction
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentTransactionRepository : JpaRepository<PaymentTransaction, Long> {
    fun existsByTransactionKey(
        transactionKey: String
    ): Boolean
}

