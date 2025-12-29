package com.ch.auction.payment.infrastructure.persistence

import com.ch.auction.payment.domain.PointTransaction
import org.springframework.data.jpa.repository.JpaRepository

interface PointTransactionRepository : JpaRepository<PointTransaction, Long>

