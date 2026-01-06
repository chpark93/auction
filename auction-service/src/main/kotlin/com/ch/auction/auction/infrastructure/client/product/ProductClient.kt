package com.ch.auction.auction.infrastructure.client.product

import com.ch.auction.common.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "PRODUCT-SERVICE",
    path = "/internal/products",
    fallback = ProductClientFallback::class
)
interface ProductClient {

    @GetMapping("/api/v1/products/{productId}")
    fun getProduct(
        @PathVariable productId: Long
    ): ApiResponse<ProductClientDtos.ProductResponse>

    @PatchMapping("/api/v1/products/{productId}/status")
    fun updateProductStatus(
        @PathVariable productId: Long,
        @RequestParam status: String
    ): ApiResponse<ProductClientDtos.ProductResponse>
}

