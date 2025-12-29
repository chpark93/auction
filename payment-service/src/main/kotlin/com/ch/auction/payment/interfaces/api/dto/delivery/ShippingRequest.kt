package com.ch.auction.payment.interfaces.api.dto.delivery

data class ShippingRequest(
    val courierCompany: String,
    val trackingNumber: String
)

