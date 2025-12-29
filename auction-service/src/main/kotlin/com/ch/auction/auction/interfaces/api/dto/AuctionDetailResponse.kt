package com.ch.auction.auction.interfaces.api.dto

import com.ch.auction.auction.domain.Auction
import com.ch.auction.auction.domain.AuctionStatus
import com.ch.auction.auction.infrastructure.client.product.ProductClientDtos
import java.time.LocalDateTime

data class AuctionDetailResponse(
    val auctionId: Long,
    val productId: Long,
    val sellerId: Long,
    
    // Auction
    val startPrice: Long,
    val currentPrice: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val status: AuctionStatus,
    val uniqueBidders: Int,
    val bidCount: Int,
    
    // Product
    val title: String,
    val description: String?,
    val category: String,
    val condition: String,
    val images: List<ProductImageDto>,
    val thumbnailUrl: String?,
    
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(
            auction: Auction,
            product: ProductClientDtos.ProductResponse,
            uniqueBidders: Int,
            bidCount: Int
        ): AuctionDetailResponse {
            return AuctionDetailResponse(
                auctionId = auction.id!!,
                productId = auction.productId,
                sellerId = auction.sellerId,
                startPrice = auction.startPrice,
                currentPrice = auction.currentPrice,
                startTime = auction.startTime,
                endTime = auction.endTime,
                status = auction.status,
                uniqueBidders = uniqueBidders,
                bidCount = bidCount,
                title = product.title,
                description = product.description,
                category = product.category,
                condition = product.condition,
                images = product.images.map { ProductImageDto.from(it) },
                thumbnailUrl = product.thumbnailUrl,
                createdAt = auction.createdAt,
                updatedAt = auction.updatedAt
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
            image: ProductClientDtos.ProductImageDto
        ): ProductImageDto {
            return ProductImageDto(
                id = image.id,
                imageUrl = image.imageUrl,
                displayOrder = image.displayOrder
            )
        }
    }
}

