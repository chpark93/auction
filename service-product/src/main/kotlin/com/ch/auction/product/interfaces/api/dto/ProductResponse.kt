package com.ch.auction.product.interfaces.api.dto

import com.ch.auction.product.domain.Product
import com.ch.auction.product.domain.ProductCategory
import com.ch.auction.product.domain.ProductCondition
import com.ch.auction.product.domain.ProductImage
import com.ch.auction.product.domain.ProductStatus
import java.time.LocalDateTime

data class ProductResponse(
    val id: Long,
    val sellerId: Long,
    val title: String,
    val description: String?,
    val category: ProductCategory,
    val condition: ProductCondition,
    val status: ProductStatus,
    val images: List<ProductImageDto>,
    val thumbnailUrl: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(
            product: Product
        ): ProductResponse {
            return ProductResponse(
                id = product.id!!,
                sellerId = product.sellerId,
                title = product.title,
                description = product.description,
                category = product.category,
                condition = product.condition,
                status = product.status,
                images = product.images.map { ProductImageDto.from(it) },
                thumbnailUrl = product.getThumbnailUrl(),
                createdAt = product.createdAt,
                updatedAt = product.updatedAt
            )
        }
    }
}

data class ProductImageDto(
    val id: Long,
    val imageUrl: String,
    val displayOrder: Int
) {
    companion object {
        fun from(
            image: ProductImage
        ): ProductImageDto {
            return ProductImageDto(
                id = image.id!!,
                imageUrl = image.imageUrl,
                displayOrder = image.displayOrder
            )
        }
    }
}
