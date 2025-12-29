package com.ch.auction.payment.infrastructure

import com.ch.auction.payment.domain.PaymentProvider
import org.springframework.stereotype.Component

@Component
class MockPaymentProvider : PaymentProvider {
    // TODO: 실제 결제 연동 로직 구현
    override fun validatePayment(
        transactionKey: String,
        amount: Long
    ): Boolean {
        return amount > 0L
    }
}