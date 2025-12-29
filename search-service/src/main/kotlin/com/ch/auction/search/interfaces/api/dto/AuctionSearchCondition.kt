package com.ch.auction.search.interfaces.api.dto

data class AuctionSearchCondition(
    val keyword: String? = null,
    val category: String? = null,
    val minPrice: Long? = null,
    val maxPrice: Long? = null,
    val status: String? = null
)

