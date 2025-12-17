package com.ch.auction.product.interfaces.api.dto

import com.ch.auction.product.domain.ProductCategory
import com.ch.auction.product.domain.ProductCondition
import jakarta.validation.constraints.NotBlank

data class ProductCreateRequest(
    @field:NotBlank(message = "상품명은 필수입니다.")
    val title: String,

    val description: String? = null,

    val category: ProductCategory,

    val condition: ProductCondition
)
