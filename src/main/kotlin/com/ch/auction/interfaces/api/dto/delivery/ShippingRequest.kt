package com.ch.auction.interfaces.api.dto.delivery

data class ShippingRequest(
    val courierCompany: String,
    val trackingNumber: String
)

