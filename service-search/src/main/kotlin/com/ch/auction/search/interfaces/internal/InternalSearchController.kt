package com.ch.auction.search.interfaces.internal

import com.ch.auction.common.ApiResponse
import com.ch.auction.search.application.service.AuctionSearchService
import com.ch.auction.search.domain.document.AuctionDocument
import com.ch.auction.search.interfaces.api.dto.AuctionStatsResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Internal Search API", description = "내부 서비스 간 통신 - 검색 API")
@RestController
@RequestMapping("/internal/search")
class InternalSearchController(
    private val auctionSearchService: AuctionSearchService
) {

    @Operation(
        summary = "경매 통계 조회",
        description = "Elasticsearch 실시간 통계 (service-admin 호출)"
    )
    @GetMapping("/stats")
    fun getStatistics(): ResponseEntity<ApiResponse<AuctionStatsResponse>> {
        val stats = auctionSearchService.getStatistics()
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = stats
            )
        )
    }
    
    @Operation(
        summary = "경매 데이터 인덱싱",
        description = "경매 데이터를 Elasticsearch에 인덱싱"
    )
    @PostMapping("/index")
    fun indexAuctions(
        @RequestBody documents: List<AuctionDocument>
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val count = auctionSearchService.indexAuctions(
            documents = documents
        )
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = mapOf(
                    "indexed" to count,
                    "total" to documents.size
                )
            )
        )
    }
    
    @Operation(
        summary = "경매 데이터 재인덱싱",
        description = "기존 인덱스를 삭제하고 새로운 데이터로 재인덱싱"
    )
    @PostMapping("/reindex")
    fun reindexAuctions(
        @RequestBody documents: List<AuctionDocument>
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val count = auctionSearchService.reindexAll(
            documents = documents
        )
        
        return ResponseEntity.ok(
            ApiResponse.ok(
                data = mapOf(
                    "indexed" to count,
                    "total" to documents.size
                )
            )
        )
    }
}

