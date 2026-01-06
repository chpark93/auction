package com.ch.auction.auction.infrastructure.client.search

import com.ch.auction.common.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "search-service", path = "/internal/search")
interface SearchClient {
    
    @PostMapping("/reindex")
    fun reindexAuctions(
        @RequestBody documents: List<AuctionDocumentDto>
    ): ApiResponse<Map<String, Any>>
}

