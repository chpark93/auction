package com.ch.auction.user.infrastructure.persistence

import com.ch.auction.user.domain.PointTransaction
import com.ch.auction.user.domain.TransactionStatus
import com.ch.auction.user.domain.TransactionType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface PointTransactionRepository : JpaRepository<PointTransaction, Long> {
    
    fun findByUserIdOrderByCreatedAtDesc(
        userId: Long,
        pageable: Pageable
    ): Page<PointTransaction>
    
    fun findByAuctionIdAndUserIdAndStatusAndType(
        auctionId: Long,
        userId: Long,
        status: TransactionStatus,
        type: TransactionType
    ): List<PointTransaction>
    
    fun findByAuctionIdAndStatus(
        auctionId: Long,
        status: TransactionStatus
    ): List<PointTransaction>
}

