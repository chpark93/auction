package com.ch.auction.product.interfaces.api

import com.ch.auction.product.application.service.ProductService
import com.ch.auction.product.domain.ProductCategory
import com.ch.auction.product.domain.ProductCondition
import com.ch.auction.product.domain.ProductStatus
import com.ch.auction.product.interfaces.api.dto.ProductResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest(ProductController::class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = [ProductController::class, ProductControllerTest.TestConfig::class])
@DisplayName("Product Controller API 테스트")
class ProductControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var productService: ProductService

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun productService(): ProductService = mockk(relaxed = true)
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - 상품 조회 성공")
    fun get_product_success() {
        // Given
        val productId = 1L
        val now = LocalDateTime.now()
        val response = ProductResponse(
            id = productId,
            sellerId = 1L,
            title = "테스트 상품",
            description = "상품 설명",
            category = ProductCategory.ELECTRONICS,
            condition = ProductCondition.NEW,
            status = ProductStatus.DRAFT,
            images = emptyList(),
            thumbnailUrl = null,
            createdAt = now,
            updatedAt = now
        )

        every { productService.getProduct(productId) } returns response

        // When & Then
        mockMvc.perform(
            get("/api/v1/products/$productId")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(productId))
            .andExpect(jsonPath("$.data.title").value(response.title))

        verify { productService.getProduct(productId) }
    }

    @Test
    @DisplayName("GET /api/v1/products/seller/{sellerId} - 판매자 상품 목록 조회")
    fun get_products_by_seller() {
        // Given
        val sellerId = 1L
        val now = LocalDateTime.now()
        val products = listOf(
            ProductResponse(
                id = 1L,
                sellerId = sellerId,
                title = "상품 1",
                description = "설명 1",
                category = ProductCategory.ELECTRONICS,
                condition = ProductCondition.NEW,
                status = ProductStatus.DRAFT,
                images = emptyList(),
                thumbnailUrl = null,
                createdAt = now,
                updatedAt = now
            ),
            ProductResponse(
                id = 2L,
                sellerId = sellerId,
                title = "상품 2",
                description = "설명 2",
                category = ProductCategory.CLOTHING,
                condition = ProductCondition.USED_GOOD,
                status = ProductStatus.DRAFT,
                images = emptyList(),
                thumbnailUrl = null,
                createdAt = now,
                updatedAt = now
            )
        )

        every { productService.getProductsBySeller(sellerId) } returns products

        // When & Then
        mockMvc.perform(
            get("/api/v1/products/seller/$sellerId")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].title").value("상품 1"))
            .andExpect(jsonPath("$.data[1].title").value("상품 2"))

        verify { productService.getProductsBySeller(sellerId) }
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - 상품 삭제")
    fun delete_product() {
        // Given
        val productId = 1L
        val sellerId = 1L
        
        every { productService.deleteProduct(productId, sellerId) } returns Unit

        // When & Then
        mockMvc.perform(
            delete("/api/v1/products/$productId")
                .header("X-User-Id", sellerId.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))

        verify { productService.deleteProduct(productId, sellerId) }
    }

    @Test
    @DisplayName("PATCH /api/v1/products/{id}/status - 상품 상태 변경")
    fun update_product_status() {
        // Given
        val productId = 1L
        val sellerId = 1L
        val newStatus = ProductStatus.REGISTERED
        val now = LocalDateTime.now()
        
        val response = ProductResponse(
            id = productId,
            sellerId = sellerId,
            title = "테스트 상품",
            description = "상품 설명",
            category = ProductCategory.ELECTRONICS,
            condition = ProductCondition.NEW,
            status = newStatus,
            images = emptyList(),
            thumbnailUrl = null,
            createdAt = now,
            updatedAt = now
        )

        every { productService.updateProductStatus(productId, sellerId, newStatus) } returns response

        // When & Then
        mockMvc.perform(
            patch("/api/v1/products/$productId/status")
                .header("X-User-Id", sellerId.toString())
                .param("status", newStatus.name)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value(newStatus.name))

        verify { productService.updateProductStatus(productId, sellerId, newStatus) }
    }
}
