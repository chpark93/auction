package com.ch.auction.infrastructure.persistence

import com.ch.auction.domain.Bid
import org.springframework.data.jpa.repository.JpaRepository

interface BidJpaRepository : JpaRepository<Bid, Long>

