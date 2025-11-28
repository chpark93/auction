package com.ch.auction.domain.payment

import java.math.BigDecimal

interface PaymentProvider {

    fun validatePayment(
        transactionKey: String,
        amount: BigDecimal
    ): Boolean
}

