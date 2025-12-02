package com.ch.auction.search.application.service

import com.ch.auction.search.domain.document.AuctionDocument
import com.ch.auction.search.interfaces.api.dto.AuctionSearchCondition
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.query.Criteria
import org.springframework.data.elasticsearch.core.query.CriteriaQuery
import org.springframework.data.elasticsearch.core.query.HighlightQuery
import org.springframework.data.elasticsearch.core.query.highlight.Highlight
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters
import org.springframework.stereotype.Service

@Service
class AuctionSearchService(
    private val elasticsearchOperations: ElasticsearchOperations
) {

    fun search(
        condition: AuctionSearchCondition,
        pageable: Pageable
    ): Page<AuctionDocument> {
        val criteria = Criteria()

        // 키워드 검색
        if (!condition.keyword.isNullOrBlank()) {
            criteria.subCriteria(
                Criteria("title").contains(condition.keyword)
                    .or("sellerName").contains(condition.keyword)
            )
        }

        // 카테고리 필터
        if (!condition.category.isNullOrBlank()) {
            criteria.and("category").`is`(condition.category)
        }

        // 상태 필터
        if (!condition.status.isNullOrBlank()) {
            criteria.and("status").`is`(condition.status)
        }

        // 가격 범위 필터
        if (condition.minPrice != null || condition.maxPrice != null) {
            val priceCriteria = Criteria("currentPrice")
            if (condition.minPrice != null) priceCriteria.greaterThanEqual(condition.minPrice)
            if (condition.maxPrice != null) priceCriteria.lessThanEqual(condition.maxPrice)

            criteria.and(priceCriteria)
        }

        val query = CriteriaQuery(criteria)
        query.setPageable<CriteriaQuery>(pageable)

        if (!condition.keyword.isNullOrBlank()) {
            query.setHighlightQuery(
                HighlightQuery(
                    Highlight(
                        HighlightParameters.builder()
                            .withPreTags("<em>")
                            .withPostTags("</em>")
                            .build(),
                        listOf(
                            HighlightField("title"),
                            HighlightField("sellerName")
                        )
                    ),
                    AuctionDocument::class.java
                )
            )
        }

        val searchHits = elasticsearchOperations.search(query, AuctionDocument::class.java)
        val documents = searchHits.map { searchHit ->
            val document = searchHit.content
            val highlightFields = searchHit.highlightFields

            val titleHighlight = highlightFields["title"]?.firstOrNull()
            val sellerNameHighlight = highlightFields["sellerName"]?.firstOrNull()

            document.copy(
                title = titleHighlight ?: document.title,
                sellerName = sellerNameHighlight ?: document.sellerName
            )
        }.toList()

        return PageImpl(documents, pageable, searchHits.totalHits)
    }
}
