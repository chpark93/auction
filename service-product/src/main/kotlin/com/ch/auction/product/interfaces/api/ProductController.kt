package com.ch.auction.product.interfaces.api

import com.ch.auction.common.ApiResponse
import com.ch.auction.product.application.service.ProductService
import com.ch.auction.product.domain.ProductStatus
import com.ch.auction.product.interfaces.api.dto.ProductCreateRequest
import com.ch.auction.product.interfaces.api.dto.ProductResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Product API", description = "상품 관리 API")
@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService
) {

    @Operation(summary = "상품 생성", description = "경매 상품 정보 및 이미지 등록 (multipart/form-data)")
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createProduct(
        @RequestHeader("X-User-Id") sellerId: Long,
        @Valid @ModelAttribute request: ProductCreateRequest,
        @RequestPart("images", required = false) images: List<MultipartFile>?
    ): ResponseEntity<ApiResponse<ProductResponse>> {
        val result = productService.createProduct(
            sellerId = sellerId,
            request = request,
            images = images ?: emptyList()
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = result
            )
        )
    }

    @Operation(summary = "상품 조회", description = "상품 ID로 상품 상세 정보 조회")
    @GetMapping("/{id}")
    fun getProduct(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<ProductResponse>> {
        val result = productService.getProduct(id)

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = result
            )
        )
    }

    @Operation(summary = "내가 등록한 상품 목록", description = "판매자가 등록한 상품 목록 조회")
    @GetMapping("/seller/{sellerId}")
    fun getProductsBySeller(
        @PathVariable sellerId: Long
    ): ResponseEntity<ApiResponse<List<ProductResponse>>> {
        val result = productService.getProductsBySeller(sellerId)

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = result
            )
        )
    }

    @Operation(summary = "상품 이미지 추가", description = "기존 상품에 이미지 추가")
    @PostMapping("/{id}/images", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun addProductImages(
        @RequestHeader("X-User-Id") sellerId: Long,
        @PathVariable id: Long,
        @RequestPart("images") images: List<MultipartFile>
    ): ResponseEntity<ApiResponse<ProductResponse>> {
        val result = productService.addProductImages(
            productId = id,
            sellerId = sellerId,
            images = images
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = result
            )
        )
    }

    @Operation(summary = "상품 이미지 삭제", description = "상품 이미지 삭제")
    @DeleteMapping("/{id}/images")
    fun removeProductImage(
        @RequestHeader("X-User-Id") sellerId: Long,
        @PathVariable id: Long,
        @RequestParam imageUrl: String
    ): ResponseEntity<ApiResponse<ProductResponse>> {
        val result = productService.removeProductImage(
            productId = id,
            sellerId = sellerId,
            imageUrl = imageUrl
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = result
            )
        )
    }

    @Operation(summary = "상품 상태 변경", description = "상품 상태 변경 (DRAFT, REGISTERED, SOLD)")
    @PatchMapping("/{id}/status")
    fun updateProductStatus(
        @RequestHeader("X-User-Id") sellerId: Long,
        @PathVariable id: Long,
        @RequestParam status: ProductStatus
    ): ResponseEntity<ApiResponse<ProductResponse>> {
        val result = productService.updateProductStatus(
            productId = id,
            sellerId = sellerId,
            status = status
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = result
            )
        )
    }

    @Operation(summary = "상품 삭제", description = "상품 및 이미지 삭제")
    @DeleteMapping("/{id}")
    fun deleteProduct(
        @RequestHeader("X-User-Id") sellerId: Long,
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        productService.deleteProduct(
            productId = id,
            sellerId = sellerId
        )

        return ResponseEntity.ok(
            ApiResponse.ok()
        )
    }
}
