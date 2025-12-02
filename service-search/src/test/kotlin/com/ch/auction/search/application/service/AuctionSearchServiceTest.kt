package com.ch.auction.search.application.service

import com.ch.auction.search.domain.document.AuctionDocument
import com.ch.auction.search.interfaces.api.dto.AuctionSearchCondition
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.SearchHitsImpl
import org.springframework.data.elasticsearch.core.TotalHitsRelation
import org.springframework.data.elasticsearch.core.query.CriteriaQuery
import org.springframework.data.elasticsearch.core.query.Query
import java.time.Duration
import java.time.LocalDateTime

class AuctionSearchServiceTest {

    private val elasticsearchOperations: ElasticsearchOperations = mockk()
    private val auctionSearchService = AuctionSearchService(elasticsearchOperations)

    @Test
    fun search_with_correct_criteria() {
        // Given
        val condition = AuctionSearchCondition(
            keyword = "test",
            category = "ELECTRONICS",
            status = "ONGOING",
            minPrice = 1000L,
            maxPrice = 5000L
        )
        val pageable = PageRequest.of(0, 10)

        val searchHit: SearchHit<AuctionDocument> = mockk()
        val document = AuctionDocument(
            id = "1",
            title = "Test Item",
            category = "ELECTRONICS",
            sellerName = "Seller",
            startPrice = 1000,
            currentPrice = 2000,
            bidCount = 1,
            status = "ONGOING",
            thumbnailUrl = "url",
            createdAt = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusDays(1)
        )

        every { searchHit.content } returns document
        every { searchHit.highlightFields } returns emptyMap()

        val searchHits = SearchHitsImpl(
            1L,
            TotalHitsRelation.EQUAL_TO,
            10.0f,
            Duration.ZERO,
            "scrollId",
            null,
            listOf(searchHit),
            null,
            null,
            null
        )
        
        val querySlot = slot<Query>()
        every { 
            elasticsearchOperations.search(capture(querySlot), eq(AuctionDocument::class.java)) 
        } returns searchHits

        // When
        val result = auctionSearchService.search(condition, pageable)

        // Then
        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.content[0].title).isEqualTo("Test Item")

        val capturedQuery = querySlot.captured as CriteriaQuery
        
        // Query Validation
        assertThat(capturedQuery.pageable).isEqualTo(pageable)
        
        // Verify highlight query
        assertThat(capturedQuery.highlightQuery).isNotNull
        assertThat(capturedQuery.highlightQuery.get().highlight.fields).hasSize(2) // title, sellerName
    }
}
