package com.ch.auction.auction.infrastructure.client.product

import com.ch.auction.common.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ProductClientFallback : ProductClient {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getProduct(
        productId: Long
    ): ApiResponse<ProductClientDtos.ProductResponse> {
        logger.error("ProductClient fallback triggered for getProduct: productId=$productId")
        throw RuntimeException("Product service is unavailable. Cannot fetch product information.")
    }

    override fun updateProductStatus(
        productId: Long,
        status: String
    ): ApiResponse<ProductClientDtos.ProductResponse> {
        logger.error("ProductClient fallback triggered for updateProductStatus: productId=$productId, status=$status")
        throw RuntimeException("Product service is unavailable. Cannot update product status.")
    }
}

