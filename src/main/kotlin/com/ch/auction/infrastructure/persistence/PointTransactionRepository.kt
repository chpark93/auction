package com.ch.auction.infrastructure.persistence

import com.ch.auction.domain.PointTransaction
import org.springframework.data.jpa.repository.JpaRepository

interface PointTransactionRepository : JpaRepository<PointTransaction, Long>

