package com.ch.auction.search.infrastructure.client.product

import com.ch.auction.common.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "service-product", url = "\${feign.client.config.service-product.url}")
interface ProductClient {

    @GetMapping("/api/v1/products/{productId}")
    fun getProduct(
        @PathVariable productId: Long
    ): ApiResponse<ProductClientDtos.ProductResponse>
}

