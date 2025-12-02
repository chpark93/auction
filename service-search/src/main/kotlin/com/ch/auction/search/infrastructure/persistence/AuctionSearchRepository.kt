package com.ch.auction.search.infrastructure.persistence

import com.ch.auction.search.domain.document.AuctionDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface AuctionSearchRepository : ElasticsearchRepository<AuctionDocument, String>

