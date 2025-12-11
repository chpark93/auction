package com.ch.auction.search.application.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval
import co.elastic.clients.elasticsearch._types.query_dsl.*
import com.ch.auction.search.domain.document.AuctionDocument
import com.ch.auction.search.interfaces.api.dto.AuctionSearchCondition
import com.ch.auction.search.interfaces.api.dto.AuctionStatsResponse
import com.ch.auction.search.interfaces.api.dto.HourlyTrend
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHitSupport
import org.springframework.stereotype.Service

@Service
class AuctionSearchService(
    private val elasticsearchOperations: ElasticsearchOperations,
    private val elasticsearchClient: ElasticsearchClient
) {
    private val logger = LoggerFactory.getLogger(javaClass)

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
                MatchQuery.of { matchQuery ->
                    matchQuery.field("title")
                        .query(condition.keyword)
                        .boost(2.0f)
                }._toQuery()
            )
            
            // sellerName
            shouldQueries.add(
                MatchQuery.of { matchQuery ->
                    matchQuery.field("sellerName")
                        .query(condition.keyword)
                }._toQuery()
            )
            
            boolQueryBuilder.must(
                BoolQuery.of { boolQuery ->
                    boolQuery.should(shouldQueries)
                        .minimumShouldMatch("1")
                }._toQuery()
            )
            hasConditions = true
        }

        // 카테고리 필터
        if (!condition.category.isNullOrBlank()) {
            boolQueryBuilder.filter(
                TermQuery.of { termQuery ->
                    termQuery.field("category")
                        .value(condition.category)
                }._toQuery()
            )
            hasConditions = true
        }

        // 상태 필터
        if (!condition.status.isNullOrBlank()) {
            boolQueryBuilder.filter(
                TermQuery.of { termQuery ->
                    termQuery.field("status")
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

        val finalQuery = if (hasConditions) {
            boolQueryBuilder.build()._toQuery()
        } else {
            Query.of { query ->
                query.matchAll { it }
            }
        }

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
    
    /**
     * Elasticsearch Aggregation 통계 조회
     */
    fun getStatistics(): AuctionStatsResponse {
        val startTime = System.currentTimeMillis()
        
        try {
            val searchResponse = elasticsearchClient.search({ searchBuilder ->
                searchBuilder.index("auctions")
                    .size(0)
                    .query { query ->
                        query.matchAll { it }
                    }
                    .aggregations("status_distribution") { aggregationBuilder ->
                        aggregationBuilder.terms { termBuilder ->
                            termBuilder.field("status.keyword").size(10)
                        }
                    }
                    .aggregations("category_distribution") { aggregationBuilder ->
                        aggregationBuilder.terms { termBuilder ->
                            termBuilder.field("category.keyword").size(20)
                        }
                    }
                    .aggregations("hourly_trend") { aggregationBuilder ->
                        aggregationBuilder.dateHistogram { dateHistogram ->
                            dateHistogram.field("createdAt")
                                .calendarInterval(CalendarInterval.Hour)
                        }
                    }
                    .aggregations("avg_price") { aggregationBuilder ->
                        aggregationBuilder.avg { avg ->
                            avg.field("currentPrice")
                        }
                    }
            }, AuctionDocument::class.java)
            
            // Status별 집계
            val statusDistribution = mutableMapOf<String, Long>()
            var totalAuctions = 0L
            var ongoingAuctions = 0L
            var completedAuctions = 0L
            
            searchResponse.aggregations()["status_distribution"]?.let { aggregation ->
                if (aggregation.isSterms) {
                    aggregation.sterms().buckets().array().forEach { bucket ->
                        val status = bucket.key().stringValue()
                        val count = bucket.docCount()

                        statusDistribution[status] = count
                        totalAuctions += count
                        
                        when (status) {
                            "ONGOING" -> ongoingAuctions = count
                            "COMPLETED", "ENDED" -> completedAuctions += count
                        }
                    }
                }
            }
            
            // Category별 집계
            val categoryDistribution = mutableMapOf<String, Long>()
            searchResponse.aggregations()["category_distribution"]?.let { aggregation ->
                if (aggregation.isSterms) {
                    aggregation.sterms().buckets().array().forEach { bucket ->
                        categoryDistribution[bucket.key().stringValue()] = bucket.docCount()
                    }
                }
            }
            
            // 시간대별 추이
            val hourlyTrend = mutableListOf<HourlyTrend>()
            searchResponse.aggregations()["hourly_trend"]?.let { aggregation ->
                if (aggregation.isDateHistogram) {
                    aggregation.dateHistogram().buckets().array().forEach { bucket ->
                        hourlyTrend.add(
                            HourlyTrend(
                                hour = bucket.keyAsString() ?: "",
                                count = bucket.docCount()
                            )
                        )
                    }
                }
            }
            
            // 평균 가격
            var avgPrice = 0.0
            searchResponse.aggregations()["avg_price"]?.let { aggregation ->
                if (aggregation.isAvg) {
                    avgPrice = aggregation.avg().value()
                }
            }
            
            val elapsedTime = System.currentTimeMillis() - startTime
            logger.info("Elasticsearch aggregation statistics fetched in ${elapsedTime}ms")
            
            return AuctionStatsResponse(
                totalAuctions = totalAuctions,
                ongoingAuctions = ongoingAuctions,
                completedAuctions = completedAuctions,
                statusDistribution = statusDistribution,
                categoryDistribution = categoryDistribution,
                hourlyRegistrationTrend = hourlyTrend,
                averageCurrentPrice = avgPrice
            )
            
        } catch (e: Exception) {
            logger.error("Failed to fetch Elasticsearch statistics", e)
 
            return AuctionStatsResponse(
                totalAuctions = 0,
                ongoingAuctions = 0,
                completedAuctions = 0,
                statusDistribution = emptyMap(),
                categoryDistribution = emptyMap(),
                hourlyRegistrationTrend = emptyList(),
                averageCurrentPrice = 0.0
            )
        }
    }
}
