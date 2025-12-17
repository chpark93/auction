package com.ch.auction.product.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var sellerId: Long,

    @Column(nullable = false)
    var title: String,

    @Lob
    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: ProductCategory,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "product_condition")
    var condition: ProductCondition,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ProductStatus = ProductStatus.DRAFT,

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    val images: MutableList<ProductImage> = mutableListOf(),

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }

    fun addImage(
        imageUrl: String,
        displayOrder: Int
    ) {
        val image = ProductImage(
            product = this,
            imageUrl = imageUrl,
            displayOrder = displayOrder
        )

        images.add(image)
    }

    fun removeImage(
        imageUrl: String
    ) {
        images.removeIf { it.imageUrl == imageUrl }
    }

    fun getThumbnailUrl(): String? {
        return images.firstOrNull { it.displayOrder == 0 }?.imageUrl
    }

    companion object {
        fun create(
            sellerId: Long,
            title: String,
            description: String?,
            category: ProductCategory,
            condition: ProductCondition,
            status: ProductStatus = ProductStatus.DRAFT
        ): Product {
            return Product(
                sellerId = sellerId,
                title = title,
                description = description,
                category = category,
                condition = condition,
                status = status
            )
        }
    }
}
