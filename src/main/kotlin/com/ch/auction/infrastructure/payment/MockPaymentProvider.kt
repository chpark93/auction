package com.ch.auction.infrastructure.payment

import com.ch.auction.domain.payment.PaymentProvider
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class MockPaymentProvider : PaymentProvider {
    override fun validatePayment(
        transactionKey: String,
        amount: BigDecimal
    ): Boolean {
        // TODO: 실제 PG사 API 호출하여 결제 금액 일치 여부 확인
        return amount > BigDecimal.ZERO
    }
}

