package com.ch.auction.search.application.service

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery
import co.elastic.clients.json.JsonData
import com.ch.auction.search.domain.document.AuctionDocument
import com.ch.auction.search.interfaces.api.dto.AuctionSearchCondition
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHitSupport
import org.springframework.stereotype.Service

@Service
class AuctionSearchService(
    private val elasticsearchOperations: ElasticsearchOperations
) {

    fun search(
        condition: AuctionSearchCondition,
        pageable: Pageable
    ): Page<AuctionDocument> {
        val boolQueryBuilder = BoolQuery.Builder()
        var hasConditions = false

        // 키워드 검색 - title / sellerName
        if (!condition.keyword.isNullOrBlank()) {
            val shouldQueries = mutableListOf<Query>()
            
            // title
            shouldQueries.add(
                MatchQuery.of { m ->
                    m.field("title")
                        .query(condition.keyword)
                        .boost(2.0f)
                }._toQuery()
            )
            
            // sellerName
            shouldQueries.add(
                MatchQuery.of { m ->
                    m.field("sellerName")
                        .query(condition.keyword)
                }._toQuery()
            )
            
            boolQueryBuilder.must(
                BoolQuery.of { b ->
                    b.should(shouldQueries)
                        .minimumShouldMatch("1")
                }._toQuery()
            )
            hasConditions = true
        }

        // 카테고리 필터
        if (!condition.category.isNullOrBlank()) {
            boolQueryBuilder.filter(
                TermQuery.of { t ->
                    t.field("category")
                        .value(condition.category)
                }._toQuery()
            )
            hasConditions = true
        }

        // 상태 필터
        if (!condition.status.isNullOrBlank()) {
            boolQueryBuilder.filter(
                TermQuery.of { t ->
                    t.field("status")
                        .value(condition.status)
                }._toQuery()
            )
            hasConditions = true
        }

        // 가격 범위 필터
        if (condition.minPrice != null || condition.maxPrice != null) {
            val rangeQuery = NumberRangeQuery.Builder()
                .field("currentPrice")
                .apply {
                    condition.minPrice?.let { minPrice ->
                        gte(minPrice.toDouble())
                    }
                    condition.maxPrice?.let { maxPrice ->
                        lte(maxPrice.toDouble())
                    }
                }
                .build()

            boolQueryBuilder.filter(rangeQuery._toRangeQuery())
            hasConditions = true
        }

        // 최종 쿼리 빌드
        val finalQuery = if (hasConditions) {
            boolQueryBuilder.build()._toQuery()
        } else {
            // 조건이 없으면 전체 검색
            Query.of { q ->
                q.matchAll { it }
            }
        }

        // NativeQuery 생성
        val searchQuery = NativeQuery.builder()
            .withQuery(finalQuery)
            .withPageable(pageable)
            .build()

        // 검색 실행
        val searchHits = elasticsearchOperations.search(searchQuery, AuctionDocument::class.java)
        val searchPage = SearchHitSupport.searchPageFor(searchHits, pageable)

        return PageImpl(
            searchPage.content.map { it.content },
            pageable,
            searchPage.totalElements
        )
    }
}
