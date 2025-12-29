package com.ch.auction.admin.infrastructure.client

import com.ch.auction.admin.infrastructure.client.dto.SearchClientDtos
import com.ch.auction.common.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

@FeignClient(name = "service-search")
interface SearchClient {
    
    @GetMapping("/internal/search/stats")
    fun getStats(): ApiResponse<SearchClientDtos.StatsResponse>
}

