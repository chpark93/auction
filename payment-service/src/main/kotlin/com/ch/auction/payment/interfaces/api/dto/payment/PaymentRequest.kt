package com.ch.auction.payment.interfaces.api.dto.payment

data class PaymentRequest(
    val transactionKey: String,
    val orderId: String,
    val amount: Long
)
