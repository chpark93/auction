package com.ch.auction.search.application.service

import com.ch.auction.search.domain.document.AuctionDocument
import com.ch.auction.search.interfaces.api.dto.AuctionSearchCondition
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.SearchHitsImpl
import org.springframework.data.elasticsearch.core.TotalHitsRelation
import org.springframework.data.elasticsearch.core.query.CriteriaQuery
import java.time.Duration
import java.time.LocalDateTime

class AuctionSearchServiceTest {

    private val elasticsearchOperations: ElasticsearchOperations = mockk()
    private lateinit var auctionSearchService: AuctionSearchService

    @BeforeEach
    fun setUp() {
        auctionSearchService = AuctionSearchService(elasticsearchOperations)
    }

    @Test
    @DisplayName("키워드로 검색 - title에 키워드가 포함된 경매를 반환")
    fun search_with_keyword_in_title() {
 // given
        val keyword = "테스트"
        val condition = AuctionSearchCondition(keyword = keyword)
        val pageable = PageRequest.of(0, 10)

        val document = createSampleDocument(
            id = "1",
            title = "테스트 경매",
            sellerName = "판매자1",
            currentPrice = 10000L
        )

        val searchHit = mockk<SearchHit<AuctionDocument>>()
        every { searchHit.content } returns document
        every { searchHit.highlightFields } returns mapOf(
            "title" to listOf("<em>테스트</em> 경매")
        )

        val searchHits = SearchHitsImpl<AuctionDocument>(
            1L, // totalHits
            TotalHitsRelation.EQUAL_TO, // totalHitsRelation
            1.0f, // maxScore
            Duration.ZERO, // executionDuration
            null, // scrollId
            null, // pointInTimeId
            listOf(searchHit), // searchHits
            null, // aggregations
            null, // suggest
            null // searchShardStatistics
        )

        every {
            elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java)
        } returns searchHits

        // when
        val result = auctionSearchService.search(
            condition = condition,
            pageable = pageable
        )

        // then
        assertEquals(1, result.totalElements)
        assertEquals(1, result.content.size)
        assertTrue(result.content[0].title.contains("테스트"))

        verify(exactly = 1) {
            elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java)
        }
    }

    @Test
    @DisplayName("키워드로 검색 - sellerName에 키워드가 포함된 경매를 반환")
    fun search_with_keyword_in_sellerName() {
        // given
        val keyword = "판매자"
        val condition = AuctionSearchCondition(keyword = keyword)
        val pageable = PageRequest.of(0, 10)

        val document = createSampleDocument(
            id = "1",
            title = "경매 아이템",
            sellerName = "판매자1",
            currentPrice = 10000L
        )

        val searchHit = mockk<SearchHit<AuctionDocument>>()
        every { searchHit.content } returns document
        every { searchHit.highlightFields } returns mapOf(
            "sellerName" to listOf("<em>판매자</em>1")
        )

        val searchHits = SearchHitsImpl<AuctionDocument>(
            1L, // totalHits
            TotalHitsRelation.EQUAL_TO, // totalHitsRelation
            1.0f, // maxScore
            Duration.ZERO, // executionDuration
            null, // scrollId
            null, // pointInTimeId
            listOf(searchHit), // searchHits
            null, // aggregations
            null, // suggest
            null // searchShardStatistics
        )

        every { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) } returns searchHits

        // when
        val result = auctionSearchService.search(condition, pageable)

        // then
        assertEquals(1, result.totalElements)
        assertEquals(1, result.content.size)
        assertTrue(result.content[0].sellerName.contains("판매자"))

        verify(exactly = 1) { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) }
    }

    @Test
    @DisplayName("카테고리로 필터링 - 특정 카테고리의 경매만 반환")
    fun search_with_category_filter() {
 // given
        val category = "전자제품"
        val condition = AuctionSearchCondition(category = category)
        val pageable = PageRequest.of(0, 10)

        val document1 = createSampleDocument(
            id = "1",
            title = "노트북",
            category = "전자제품",
            currentPrice = 100000L
        )

        val searchHit = mockk<SearchHit<AuctionDocument>>()
        every { searchHit.content } returns document1
        every { searchHit.highlightFields } returns emptyMap()

        val searchHits = SearchHitsImpl<AuctionDocument>(
            1L, // totalHits
            TotalHitsRelation.EQUAL_TO, // totalHitsRelation
            1.0f, // maxScore
            Duration.ZERO, // executionDuration
            null, // scrollId
            null, // pointInTimeId
            listOf(searchHit), // searchHits
            null, // aggregations
            null, // suggest
            null // searchShardStatistics
        )

        every { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) } returns searchHits

 // when
        val result = auctionSearchService.search(condition, pageable)

 // then
        assertEquals(1, result.totalElements)
        assertEquals(category, result.content[0].category)

        verify(exactly = 1) { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) }
    }

    @Test
    @DisplayName("가격 범위로 필터링 - minPrice와 maxPrice 사이의 경매만 반환")
    fun search_with_price_range_filter() {
 // given
        val condition = AuctionSearchCondition(
            minPrice = 10000L,
            maxPrice = 50000L
        )
        val pageable = PageRequest.of(0, 10)

        val document = createSampleDocument(
            id = "1",
            title = "중간 가격 아이템",
            currentPrice = 30000L
        )

        val searchHit = mockk<SearchHit<AuctionDocument>>()
        every { searchHit.content } returns document
        every { searchHit.highlightFields } returns emptyMap()

        val searchHits = SearchHitsImpl<AuctionDocument>(
            1L, // totalHits
            TotalHitsRelation.EQUAL_TO, // totalHitsRelation
            1.0f, // maxScore
            Duration.ZERO, // executionDuration
            null, // scrollId
            null, // pointInTimeId
            listOf(searchHit), // searchHits
            null, // aggregations
            null, // suggest
            null // searchShardStatistics
        )

        every { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) } returns searchHits

 // when
        val result = auctionSearchService.search(condition, pageable)

 // then
        assertEquals(1, result.totalElements)
        assertTrue(result.content[0].currentPrice >= 10000L)
        assertTrue(result.content[0].currentPrice <= 50000L)

        verify(exactly = 1) { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) }
    }

    @Test
    @DisplayName("상태로 필터링 - 특정 상태의 경매만 반환")
    fun search_with_status_filter() {
 // given
        val status = "ONGOING"
        val condition = AuctionSearchCondition(status = status)
        val pageable = PageRequest.of(0, 10)

        val document = createSampleDocument(
            id = "1",
            title = "진행 중인 경매",
            status = status,
            currentPrice = 20000L
        )

        val searchHit = mockk<SearchHit<AuctionDocument>>()
        every { searchHit.content } returns document
        every { searchHit.highlightFields } returns emptyMap()

        val searchHits = SearchHitsImpl<AuctionDocument>(
            1L, // totalHits
            TotalHitsRelation.EQUAL_TO, // totalHitsRelation
            1.0f, // maxScore
            Duration.ZERO, // executionDuration
            null, // scrollId
            null, // pointInTimeId
            listOf(searchHit), // searchHits
            null, // aggregations
            null, // suggest
            null // searchShardStatistics
        )

        every { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) } returns searchHits

 // when
        val result = auctionSearchService.search(condition, pageable)

 // then
        assertEquals(1, result.totalElements)
        assertEquals(status, result.content[0].status)

        verify(exactly = 1) { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) }
    }

    @Test
    @DisplayName("복합 조건 검색 - 키워드, 카테고리, 가격 범위, 상태를 모두 적용")
    fun search_with_multiple_conditions() {
 // given
        val condition = AuctionSearchCondition(
            keyword = "노트북",
            category = "전자제품",
            minPrice = 50000L,
            maxPrice = 200000L,
            status = "ONGOING"
        )
        val pageable = PageRequest.of(0, 10)

        val document = createSampleDocument(
            id = "1",
            title = "고성능 노트북",
            category = "전자제품",
            status = "ONGOING",
            currentPrice = 150000L
        )

        val searchHit = mockk<SearchHit<AuctionDocument>>()
        every { searchHit.content } returns document
        every { searchHit.highlightFields } returns mapOf(
            "title" to listOf("고성능 <em>노트북</em>")
        )

        val searchHits = SearchHitsImpl<AuctionDocument>(
            1L, // totalHits
            TotalHitsRelation.EQUAL_TO, // totalHitsRelation
            1.0f, // maxScore
            Duration.ZERO, // executionDuration
            null, // scrollId
            null, // pointInTimeId
            listOf(searchHit), // searchHits
            null, // aggregations
            null, // suggest
            null // searchShardStatistics
        )

        every { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) } returns searchHits

 // when
        val result = auctionSearchService.search(condition, pageable)

 // then
        assertEquals(1, result.totalElements)
        val resultDocument = result.content[0]
        assertTrue(resultDocument.title.contains("노트북"))
        assertEquals("전자제품", resultDocument.category)
        assertEquals("ONGOING", resultDocument.status)
        assertTrue(resultDocument.currentPrice >= 50000L)
        assertTrue(resultDocument.currentPrice <= 200000L)

        verify(exactly = 1) { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) }
    }

    @Test
    @DisplayName("검색 결과 없음 - 조건에 맞는 경매가 없을 때 빈 페이지 반환")
    fun search_no_results() {
 // given
        val condition = AuctionSearchCondition(keyword = "존재하지않는키워드")
        val pageable = PageRequest.of(0, 10)

        val searchHits = SearchHitsImpl<AuctionDocument>(
            0L, // totalHits
            TotalHitsRelation.EQUAL_TO, // totalHitsRelation
            0f, // maxScore
            Duration.ZERO, // executionDuration
            null, // scrollId
            null, // pointInTimeId
            emptyList(), // searchHits
            null, // aggregations
            null, // suggest
            null // searchShardStatistics
        )

        every { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) } returns searchHits

 // when
        val result = auctionSearchService.search(condition, pageable)

 // then
        assertEquals(0, result.totalElements)
        assertTrue(result.content.isEmpty())

        verify(exactly = 1) { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) }
    }

    @Test
    @DisplayName("하이라이트 적용 - 키워드가 있을 때 제목과 판매자명에 하이라이트 태그 포함")
    fun search_with_highlight() {
 // given
        val keyword = "맥북"
        val condition = AuctionSearchCondition(keyword = keyword)
        val pageable = PageRequest.of(0, 10)

        val document = createSampleDocument(
            id = "1",
            title = "맥북 프로 2023",
            sellerName = "애플 스토어",
            currentPrice = 2000000L
        )

        val searchHit = mockk<SearchHit<AuctionDocument>>()
        every { searchHit.content } returns document
        every { searchHit.highlightFields } returns mapOf(
            "title" to listOf("<em>맥북</em> 프로 2023")
        )

        val searchHits = SearchHitsImpl<AuctionDocument>(
            1L, // totalHits
            TotalHitsRelation.EQUAL_TO, // totalHitsRelation
            1.0f, // maxScore
            Duration.ZERO, // executionDuration
            null, // scrollId
            null, // pointInTimeId
            listOf(searchHit), // searchHits
            null, // aggregations
            null, // suggest
            null // searchShardStatistics
        )

        every { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) } returns searchHits

 // when
        val result = auctionSearchService.search(condition, pageable)

 // then
        assertEquals(1, result.totalElements)
        assertTrue(result.content[0].title.contains("<em>"))
        assertTrue(result.content[0].title.contains("</em>"))

        verify(exactly = 1) { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) }
    }

    @Test
    @DisplayName("페이징 처리 - 지정된 페이지 크기로 결과 반환")
    fun search_with_pagination() {
 // given
        val condition = AuctionSearchCondition()
        val pageable = PageRequest.of(0, 5) // 페이지 크기 5

        val documents = (1..5).map { idx ->
            val doc = createSampleDocument(
                id = idx.toString(),
                title = "경매 $idx",
                currentPrice = 10000L * idx
            )
            val searchHit = mockk<SearchHit<AuctionDocument>>()
            every { searchHit.content } returns doc
            every { searchHit.highlightFields } returns emptyMap()
            searchHit
        }

        val searchHits = SearchHitsImpl<AuctionDocument>(
            10L, // totalHits (총 10개의 결과가 있다고 가정)
            TotalHitsRelation.EQUAL_TO, // totalHitsRelation
            1.0f, // maxScore
            Duration.ZERO, // executionDuration
            null, // scrollId
            null, // pointInTimeId
            documents, // searchHits
            null, // aggregations
            null, // suggest
            null // searchShardStatistics
        )

        every { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) } returns searchHits

 // when
        val result = auctionSearchService.search(condition, pageable)

 // then
        assertEquals(10, result.totalElements) // 전체 결과 수
        assertEquals(5, result.content.size) // 현재 페이지 결과 수
        assertEquals(2, result.totalPages) // 총 페이지 수

        verify(exactly = 1) { elasticsearchOperations.search(any<CriteriaQuery>(), AuctionDocument::class.java) }
    }

    private fun createSampleDocument(
        id: String,
        title: String,
        sellerName: String = "테스트 판매자",
        category: String = "기타",
        status: String = "ONGOING",
        currentPrice: Long
    ): AuctionDocument {
        return AuctionDocument(
            id = id,
            title = title,
            category = category,
            sellerName = sellerName,
            startPrice = currentPrice,
            currentPrice = currentPrice,
            bidCount = 0,
            status = status,
            thumbnailUrl = null,
            createdAt = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusDays(7)
        )
    }
}
