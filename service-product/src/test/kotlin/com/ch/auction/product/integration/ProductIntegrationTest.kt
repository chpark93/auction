package com.ch.auction.product.integration

import com.ch.auction.product.domain.ProductCategory
import com.ch.auction.product.domain.ProductCondition
import com.ch.auction.product.infrastructure.persistence.ProductJpaRepository
import com.ch.auction.product.interfaces.api.dto.ProductCreateRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Product 통합 테스트")
class ProductIntegrationTest @Autowired constructor(
    private val productJpaRepository: ProductJpaRepository
) {

    @AfterEach
    fun tearDown() {
        productJpaRepository.deleteAll()
    }

    @Test
    @DisplayName("상품이 정상적으로 저장되는지 확인")
    fun `should save product successfully`() {
        // Given
        val sellerId = 1L
        val product = com.ch.auction.product.domain.Product.create(
            sellerId = sellerId,
            title = "테스트 상품",
            description = "상품 설명",
            category = ProductCategory.ELECTRONICS,
            condition = ProductCondition.NEW
        )

        // When
        val savedProduct = productJpaRepository.save(product)

        // Then
        assertNotNull(savedProduct)
        assertNotNull(savedProduct.id)
        assertEquals("테스트 상품", savedProduct.title)
        assertEquals(ProductCategory.ELECTRONICS, savedProduct.category)
    }

    @Test
    @DisplayName("이미지가 포함된 상품이 정상적으로 저장되는지 확인")
    fun `should save product with images successfully`() {
        // Given
        val sellerId = 1L
        val product = com.ch.auction.product.domain.Product.create(
            sellerId = sellerId,
            title = "테스트 상품",
            description = "상품 설명",
            category = ProductCategory.ELECTRONICS,
            condition = ProductCondition.NEW
        )

        product.addImage("http://example.com/image1.jpg", 0)
        product.addImage("http://example.com/image2.jpg", 1)

        // When
        val savedProduct = productJpaRepository.save(product)

        // Then
        assertNotNull(savedProduct)
        assertNotNull(savedProduct.id)
        assertEquals(2, savedProduct.images.size)
        assertEquals("http://example.com/image1.jpg", savedProduct.images[0].imageUrl)
        assertEquals(0, savedProduct.images[0].displayOrder)
    }

    @Test
    @DisplayName("판매자의 상품 목록을 조회할 수 있는지 확인")
    fun `should find products by seller`() {
        // Given
        val sellerId = 1L
        val product1 = com.ch.auction.product.domain.Product.create(
            sellerId = sellerId,
            title = "상품 1",
            description = "설명 1",
            category = ProductCategory.ELECTRONICS,
            condition = ProductCondition.NEW
        )
        val product2 = com.ch.auction.product.domain.Product.create(
            sellerId = sellerId,
            title = "상품 2",
            description = "설명 2",
            category = ProductCategory.CLOTHING,
            condition = ProductCondition.USED_GOOD
        )

        productJpaRepository.save(product1)
        productJpaRepository.save(product2)

        // When
        val products = productJpaRepository.findBySellerId(sellerId)

        // Then
        assertNotNull(products)
        assertEquals(2, products.size)
        assertTrue(products.any { it.title == "상품 1" })
        assertTrue(products.any { it.title == "상품 2" })
    }

    @Test
    @DisplayName("이미지를 포함한 상품 조회가 정상적으로 동작하는지 확인")
    fun `should find product with images`() {
        // Given
        val sellerId = 1L
        val product = com.ch.auction.product.domain.Product.create(
            sellerId = sellerId,
            title = "테스트 상품",
            description = "상품 설명",
            category = ProductCategory.ELECTRONICS,
            condition = ProductCondition.NEW
        )

        product.addImage("http://example.com/image1.jpg", 0)
        product.addImage("http://example.com/image2.jpg", 1)

        val savedProduct = productJpaRepository.save(product)

        // When
        val foundProduct = productJpaRepository.findByIdWithImages(savedProduct.id!!)

        // Then
        assertNotNull(foundProduct)
        assertEquals(2, foundProduct!!.images.size)
        assertEquals("http://example.com/image1.jpg", foundProduct.images[0].imageUrl)
    }
}

