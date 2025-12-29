package com.ch.auction.search.infrastructure.client.product

import java.time.LocalDateTime

object ProductClientDtos {
    data class ProductResponse(
        val id: Long,
        val sellerId: Long,
        val title: String,
        val description: String?,
        val category: String,
        val condition: String,
        val status: String,
        val images: List<ProductImageDto>,
        val thumbnailUrl: String?,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )

    data class ProductImageDto(
        val id: Long,
        val imageUrl: String,
        val displayOrder: Int
    )
}

