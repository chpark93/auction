package com.ch.auction.product.infrastructure.persistence

import com.ch.auction.product.domain.Product
import com.ch.auction.product.domain.ProductStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ProductJpaRepository : JpaRepository<Product, Long> {
    fun findBySellerId(
        sellerId: Long
    ): List<Product>

    fun findBySellerIdAndStatus(
        sellerId: Long,
        status: ProductStatus
    ): List<Product>
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
    fun findByIdWithImages(
        id: Long
    ): Product?
}
