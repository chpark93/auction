package com.ch.auction.interfaces.api.dto.payment

import java.math.BigDecimal

data class PaymentRequest(
    val transactionKey: String,
    val orderId: String,
    val amount: BigDecimal
)
