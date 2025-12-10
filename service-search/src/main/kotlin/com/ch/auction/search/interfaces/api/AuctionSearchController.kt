package com.ch.auction.search.interfaces.api

import com.ch.auction.common.ApiResponse
import com.ch.auction.search.application.service.AuctionSearchService
import com.ch.auction.search.domain.document.AuctionDocument
import com.ch.auction.search.interfaces.api.dto.AuctionSearchCondition
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/search")
class AuctionSearchController(
    private val auctionSearchService: AuctionSearchService
) {

    @GetMapping("/auctions")
    fun search(
        @ModelAttribute condition: AuctionSearchCondition,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<AuctionDocument>>> {
        val result = auctionSearchService.search(
            condition = condition,
            pageable = pageable
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = result
            )
        )
    }
}

