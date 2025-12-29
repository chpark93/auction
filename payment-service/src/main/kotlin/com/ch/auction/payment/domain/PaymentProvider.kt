package com.ch.auction.payment.domain

interface PaymentProvider {

    fun validatePayment(
        transactionKey: String,
        amount: Long
    ): Boolean
}