package com.ch.auction.product.application.service

import com.ch.auction.exception.BusinessException
import com.ch.auction.product.domain.Product
import com.ch.auction.product.domain.ProductCategory
import com.ch.auction.product.domain.ProductCondition
import com.ch.auction.product.domain.ProductImage
import com.ch.auction.product.domain.ProductStatus
import com.ch.auction.product.infrastructure.event.ProductEventPublisher
import com.ch.auction.product.infrastructure.persistence.ProductJpaRepository
import com.ch.auction.product.interfaces.api.dto.ProductCreateRequest
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile
import java.lang.reflect.Field
import java.util.*

@DisplayName("Product Service 테스트")
class ProductServiceTest {

    private lateinit var productService: ProductService
    private lateinit var productRepository: ProductJpaRepository
    private lateinit var imageUploadService: ImageUploadService
    private lateinit var productEventPublisher: ProductEventPublisher

    @BeforeEach
    fun setUp() {
        productRepository = mockk(relaxed = true)
        imageUploadService = mockk(relaxed = true)
        productEventPublisher = mockk(relaxed = true)
        
        productService = ProductService(
            productJpaRepository = productRepository,
            imageUploadService = imageUploadService,
            productEventPublisher = productEventPublisher
        )
    }

    @Test
    @DisplayName("상품 생성 - 이미지 포함")
    fun create_product_with_images_success() {
        // Given
        val sellerId = 1L
        val request = ProductCreateRequest(
            title = "테스트 상품",
            description = "상품 설명",
            category = ProductCategory.ELECTRONICS,
            condition = ProductCondition.NEW
        )
        
        val images = listOf(
            MockMultipartFile("images", "test1.jpg", "image/jpeg", "test content 1".toByteArray()),
            MockMultipartFile("images", "test2.jpg", "image/jpeg", "test content 2".toByteArray())
        )

        val imageUrls = listOf(
            "http://localhost:9000/auction-bucket/products/image1.jpg",
            "http://localhost:9000/auction-bucket/products/image2.jpg"
        )

        val savedProduct = Product.create(
            sellerId = sellerId,
            title = request.title,
            description = request.description,
            category = request.category,
            condition = request.condition
        )
        setProductId(savedProduct, 1L)

        every { imageUploadService.uploadFiles(images, "products") } returns imageUrls
        every { productRepository.save(any()) } answers {
            val product = firstArg<Product>()
            setProductId(product, 1L)

            product.images.forEachIndexed { index, img ->
                setImageId(img, (index + 1).toLong())
            }

            product
        }

        // When
        val result = productService.createProduct(sellerId, request, images)

        // Then
        assertNotNull(result)
        assertEquals(savedProduct.title, result.title)
        
        verify { imageUploadService.uploadFiles(images, "products") }
        verify(atLeast = 1) { productRepository.save(any()) }
    }

    @Test
    @DisplayName("상품 생성 - 이미지 없이")
    fun create_product_without_image() {
        // Given
        val sellerId = 1L
        val request = ProductCreateRequest(
            title = "테스트 상품",
            description = "상품 설명",
            category = ProductCategory.ELECTRONICS,
            condition = ProductCondition.NEW
        )

        val savedProduct = Product.create(
            sellerId = sellerId,
            title = request.title,
            description = request.description,
            category = request.category,
            condition = request.condition
        )

        every { productRepository.save(any()) } answers {
            val product = firstArg<Product>()
            setProductId(product, 1L)
            product
        }

        // When
        val result = productService.createProduct(sellerId, request, emptyList())

        // Then
        assertNotNull(result)
        assertEquals(savedProduct.title, result.title)
        
        verify(exactly = 0) { imageUploadService.uploadFiles(any(), any()) }
        verify { productRepository.save(any()) }
    }

    @Test
    @DisplayName("상품 조회 - 성공")
    fun get_product_success() {
        // Given
        val productId = 1L
        val product = Product.create(
            sellerId = 1L,
            title = "테스트 상품",
            description = "상품 설명",
            category = ProductCategory.ELECTRONICS,
            condition = ProductCondition.NEW
        )
        setProductId(product, productId)

        every { productRepository.findByIdWithImages(productId) } returns product

        // When
        val result = productService.getProduct(productId)

        // Then
        assertNotNull(result)
        assertEquals(product.title, result.title)
        verify { productRepository.findByIdWithImages(productId) }
    }

    @Test
    @DisplayName("상품 조회 - 존재하지 않음")
    fun exception_when_product_not_found() {
        // Given
        val productId = 999L
        every { productRepository.findByIdWithImages(productId) } returns null

        // When & Then
        assertThrows<BusinessException> {
            productService.getProduct(productId)
        }
    }

    @Test
    @DisplayName("상품 상태 변경 - REGISTERED")
    fun update_product_status_REGISTERED() {
        // Given
        val productId = 1L
        val sellerId = 1L
        val product = Product.create(
            sellerId = sellerId,
            title = "테스트 상품",
            description = "상품 설명",
            category = ProductCategory.ELECTRONICS,
            condition = ProductCondition.NEW
        )
        setProductId(product, productId)

        every { productRepository.findById(productId) } returns Optional.of(product)
        every { productRepository.save(any()) } answers {
            val p = firstArg<Product>()
            setProductId(p, productId)
            p
        }

        // When
        val result = productService.updateProductStatus(productId, sellerId, ProductStatus.REGISTERED)

        // Then
        assertNotNull(result)
        assertEquals(ProductStatus.REGISTERED, product.status)
        verify { productRepository.save(product) }
    }

    @Test
    @DisplayName("판매자의 상품 목록 조회")
    fun get_products_by_seller() {
        // Given
        val sellerId = 1L
        val product1 = Product.create(
            sellerId = sellerId,
            title = "상품 1",
            description = "설명 1",
            category = ProductCategory.ELECTRONICS,
            condition = ProductCondition.NEW
        )
        setProductId(product1, 1L)
        
        val product2 = Product.create(
            sellerId = sellerId,
            title = "상품 2",
            description = "설명 2",
            category = ProductCategory.CLOTHING,
            condition = ProductCondition.USED_GOOD
        )
        setProductId(product2, 2L)

        val products = listOf(product1, product2)

        every { productRepository.findBySellerId(sellerId) } returns products

        // When
        val result = productService.getProductsBySeller(sellerId)

        // Then
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("상품 1", result[0].title)
        assertEquals("상품 2", result[1].title)
        verify { productRepository.findBySellerId(sellerId) }
    }

    @Test
    @DisplayName("상품 삭제 - 성공")
    fun delete_product_success() {
        // Given
        val productId = 1L
        val sellerId = 1L
        val product = Product.create(
            sellerId = sellerId,
            title = "테스트 상품",
            description = "상품 설명",
            category = ProductCategory.ELECTRONICS,
            condition = ProductCondition.NEW
        )
        setProductId(product, productId)
        product.addImage("http://example.com/image1.jpg", 0)
        product.addImage("http://example.com/image2.jpg", 1)

        product.images.forEachIndexed { index, img ->
            setImageId(img, (index + 1).toLong())
        }

        every { productRepository.findByIdWithImages(productId) } returns product
        every { imageUploadService.deleteFiles(any()) } returns 2
        every { productRepository.delete(product) } returns Unit

        // When
        productService.deleteProduct(productId, sellerId)

        // Then
        verify { imageUploadService.deleteFiles(any()) }
        verify { productRepository.delete(product) }
    }

    @Test
    @DisplayName("이미지 추가 - 성공")
    fun add_product_image_success() {
        // Given
        val productId = 1L
        val sellerId = 1L
        val product = Product.create(
            sellerId = sellerId,
            title = "테스트 상품",
            description = "상품 설명",
            category = ProductCategory.ELECTRONICS,
            condition = ProductCondition.NEW
        )
        setProductId(product, productId)

        val images = listOf(
            MockMultipartFile("image", "test.jpg", "image/jpeg", "test content".toByteArray())
        )
        val imageUrls = listOf("http://localhost:9000/auction-bucket/products/image.jpg")

        every { productRepository.findByIdWithImages(productId) } returns product
        every { imageUploadService.uploadFiles(images, "products") } returns imageUrls
        every { productRepository.save(any()) } answers {
            val p = firstArg<Product>()
            setProductId(p, productId)
            p.images.forEachIndexed { index, img ->
                setImageId(img, (index + 1).toLong())
            }

            p
        }

        // When
        val result = productService.addProductImages(productId, sellerId, images)

        // Then
        assertNotNull(result)
        
        verify { imageUploadService.uploadFiles(images, "products") }
        verify { productRepository.save(any()) }
        verify { productEventPublisher.publishProductUpdated(productId) }
    }

    @Test
    @DisplayName("이미지 삭제 - 성공")
    fun remove_product_image_success() {
        // Given
        val productId = 1L
        val sellerId = 1L
        val imageUrl = "http://example.com/image1.jpg"
        val product = Product.create(
            sellerId = sellerId,
            title = "테스트 상품",
            description = "상품 설명",
            category = ProductCategory.ELECTRONICS,
            condition = ProductCondition.NEW
        )
        setProductId(product, productId)
        product.addImage(imageUrl, 0)
        product.addImage("http://example.com/image2.jpg", 1)

        product.images.forEachIndexed { index, img ->
            setImageId(img, (index + 1).toLong())
        }

        every { productRepository.findByIdWithImages(productId) } returns product
        every { imageUploadService.deleteFile(imageUrl) } returns true
        every { productRepository.save(any()) } answers {
            val p = firstArg<Product>()
            setProductId(p, productId)
            p.images.forEachIndexed { index, img ->
                if (img.id == null) {
                    setImageId(img, (index + 1).toLong())
                }
            }

            p
        }

        // When
        val result = productService.removeProductImage(productId, sellerId, imageUrl)

        // Then
        assertNotNull(result)
        
        verify { imageUploadService.deleteFile(imageUrl) }
        verify { productRepository.save(any()) }
        verify { productEventPublisher.publishProductUpdated(productId) }
    }

    @Test
    @DisplayName("상품 상태 변경 - 권한 없음")
    fun exception_when_update_status_without_permission() {
        // Given
        val productId = 1L
        val sellerId = 1L
        val wrongSellerId = 2L
        val product = Product.create(
            sellerId = sellerId,
            title = "테스트 상품",
            description = "상품 설명",
            category = ProductCategory.ELECTRONICS,
            condition = ProductCondition.NEW
        )
        setProductId(product, productId)

        every { productRepository.findById(productId) } returns Optional.of(product)

        // When & Then
        assertThrows<BusinessException> {
            productService.updateProductStatus(productId, wrongSellerId, ProductStatus.REGISTERED)
        }
    }

    private fun setProductId(
        product: Product,
        id: Long
    ) {
        val idField: Field = Product::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(product, id)
    }

    private fun setImageId(
        image: ProductImage,
        id: Long
    ) {
        val idField: Field = ProductImage::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(image, id)
    }
}
