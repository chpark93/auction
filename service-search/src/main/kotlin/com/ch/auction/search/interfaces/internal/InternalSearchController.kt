package com.ch.auction.search.interfaces.internal

import com.ch.auction.common.ApiResponse
import com.ch.auction.search.application.service.AuctionSearchService
import com.ch.auction.search.interfaces.api.dto.AuctionStatsResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
}

