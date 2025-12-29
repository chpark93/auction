package com.ch.auction.product.application.service

import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import com.ch.auction.product.domain.Product
import com.ch.auction.product.domain.ProductStatus
import com.ch.auction.product.infrastructure.event.ProductEventPublisher
import com.ch.auction.product.infrastructure.persistence.ProductJpaRepository
import com.ch.auction.product.interfaces.api.dto.ProductCreateRequest
import com.ch.auction.product.interfaces.api.dto.ProductResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional(readOnly = true)
class ProductService(
    private val productJpaRepository: ProductJpaRepository,
    private val imageUploadService: ImageUploadService,
    private val productEventPublisher: ProductEventPublisher
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 상품 생성
     */
    @Transactional
    fun createProduct(
        sellerId: Long,
        request: ProductCreateRequest,
        images: List<MultipartFile>
    ): ProductResponse {
        val product = Product.create(
            sellerId = sellerId,
            title = request.title,
            description = request.description,
            category = request.category,
            condition = request.condition,
            status = ProductStatus.DRAFT
        )

        val savedProduct = productJpaRepository.save(product)

        if (images.isNotEmpty()) {
            val imageUrls = imageUploadService.uploadFiles(
                files = images,
                folder = "products"
            )

            imageUrls.forEachIndexed { index, imageUrl ->
                savedProduct.addImage(
                    imageUrl = imageUrl,
                    displayOrder = index
                )
            }

            productJpaRepository.save(savedProduct)
        }

        logger.info("Product created: ${savedProduct.id}")

        return ProductResponse.from(savedProduct)
    }

    /**
     * 상품 조회
     */
    fun getProduct(
        productId: Long
    ): ProductResponse {
        val product = productJpaRepository.findByIdWithImages(
            id = productId
        ) ?: throw BusinessException(ErrorCode.RESOURCE_NOT_FOUND)

        return ProductResponse.from(
            product = product
        )
    }

    /**
     * 판매자의 상품 목록 조회
     */
    fun getProductsBySeller(
        sellerId: Long
    ): List<ProductResponse> {
        val products = productJpaRepository.findBySellerId(
            sellerId = sellerId
        )

        return products.map { product ->
            ProductResponse.from(
                product = product
            )
        }
    }

    /**
     * 상품 이미지 추가
     */
    @Transactional
    fun addProductImages(
        productId: Long,
        sellerId: Long,
        images: List<MultipartFile>
    ): ProductResponse {
        val product = productJpaRepository.findByIdWithImages(
            id = productId
        ) ?: throw BusinessException(ErrorCode.RESOURCE_NOT_FOUND)

        if (product.sellerId != sellerId) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        val imageUrls = imageUploadService.uploadFiles(
            files = images,
            folder = "products"
        )

        val currentMaxOrder = product.images.maxOfOrNull { it.displayOrder } ?: -1

        imageUrls.forEachIndexed { index, imageUrl ->
            product.addImage(
                imageUrl =  imageUrl,
                displayOrder = currentMaxOrder + index + 1
            )
        }

        val savedProduct = productJpaRepository.save(product)
        logger.info("Images added to product: $productId")

        // 이벤트 발행
        productEventPublisher.publishProductUpdated(
            productId = productId
        )

        return ProductResponse.from(
            product = savedProduct
        )
    }

    /**
     * 상품 이미지 삭제
     */
    @Transactional
    fun removeProductImage(
        productId: Long,
        sellerId: Long,
        imageUrl: String
    ): ProductResponse {
        val product = productJpaRepository.findByIdWithImages(
            id = productId
        ) ?: throw BusinessException(ErrorCode.RESOURCE_NOT_FOUND)

        if (product.sellerId != sellerId) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        product.removeImage(
            imageUrl = imageUrl
        )

        imageUploadService.deleteFile(
            fileUrl = imageUrl
        )

        val savedProduct = productJpaRepository.save(product)
        logger.info("Image removed from product: $productId")

        // 이벤트 발행
        productEventPublisher.publishProductUpdated(
            productId = productId
        )

        return ProductResponse.from(
            product = savedProduct
        )
    }

    /**
     * 상품 상태 변경
     */
    @Transactional
    fun updateProductStatus(
        productId: Long,
        sellerId: Long,
        status: ProductStatus
    ): ProductResponse {
        val product = productJpaRepository.findById(productId)
            .orElseThrow { BusinessException(ErrorCode.RESOURCE_NOT_FOUND) }

        if (product.sellerId != sellerId) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        product.status = status
        val savedProduct = productJpaRepository.save(product)
        logger.info("Product status updated: $productId -> $status")

        return ProductResponse.from(
            product = savedProduct
        )
    }

    /**
     * 상품 삭제
     */
    @Transactional
    fun deleteProduct(
        productId: Long,
        sellerId: Long
    ) {
        val product = productJpaRepository.findByIdWithImages(
            id = productId
        ) ?: throw BusinessException(ErrorCode.RESOURCE_NOT_FOUND)

        if (product.sellerId != sellerId) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        val imageUrls = product.images.map { it.imageUrl }
        if (imageUrls.isNotEmpty()) {
            imageUploadService.deleteFiles(imageUrls)
        }

        productJpaRepository.delete(product)
        logger.info("Product deleted: $productId")
    }
}
