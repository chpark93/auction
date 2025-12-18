package com.ch.auction.search.interfaces.api

import com.ch.auction.search.application.service.AuctionSearchService
import com.ch.auction.search.domain.document.AuctionDocument
import com.ch.auction.search.interfaces.api.dto.AuctionSearchCondition
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@WebMvcTest(AuctionSearchController::class)
@AutoConfigureMockMvc(addFilters = false)
class AuctionSearchControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var auctionSearchService: AuctionSearchService

    @Test
    fun search_auctions_results() {
        // given
        val document = AuctionDocument(
            id = "1",
            productId = 1L,
            title = "Test Item",
            description = null,
            category = "ELECTRONICS",
            condition = "NEW",
            sellerName = "Seller",
            sellerId = 1L,
            startPrice = 1000,
            currentPrice = 2000,
            bidCount = 1,
            status = "ONGOING",
            thumbnailUrl = "url",
            createdAt = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusDays(1)
        )
        val page = PageImpl(listOf(document))

        every { 
            auctionSearchService.search(any<AuctionSearchCondition>(), any<Pageable>()) 
        } returns page

        // when & then
        mockMvc.perform(
            get("/api/v1/search/auctions")
                .param("keyword", "test")
                .param("category", "ELECTRONICS")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content[0].title").value("Test Item"))
            .andExpect(jsonPath("$.data.totalElements").value(1))
    }
}
