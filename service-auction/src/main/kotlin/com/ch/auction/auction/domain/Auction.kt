package com.ch.auction.auction.domain

import com.ch.auction.exception.BusinessException
import com.ch.auction.common.ErrorCode
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@Table(name = "auctions")
@SQLRestriction("deleted = false")
class Auction private constructor(
    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var startPrice: Long,

    @Column(nullable = false)
    var startTime: LocalDateTime,

    @Column(nullable = false)
    var endTime: LocalDateTime,

    @Column(nullable = false)
    val sellerId: Long,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
    @Column(nullable = false)
    var currentPrice: Long = startPrice
        private set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AuctionStatus = AuctionStatus.PENDING
        private set

    @Column(nullable = false)
    var deleted: Boolean = false
        private set

    companion object {
        fun create(
            title: String,
            startPrice: Long,
            startTime: LocalDateTime,
            endTime: LocalDateTime,
            sellerId: Long
        ): Auction {
            return Auction(
                title = title,
                startPrice = startPrice,
                startTime = startTime,
                endTime = endTime,
                sellerId = sellerId
            )
        }
    }

    /**
     * 입찰
     */
    fun placeBid(
        userId: Long,
        amount: Long,
        currentTime: LocalDateTime = LocalDateTime.now(),
        sequence: Long = 0
    ): Bid {
        // 상태 체크
        verifyStatus()

        // 시간 체크
        verifyTime(
            currentTime = currentTime
        )

        // 가격 체크
        verifyPrice(
            amount = amount
        )

        this.currentPrice = amount

        // Bid 생성
        return Bid.create(
            auctionId = this.id ?: throw BusinessException(ErrorCode.AUCTION_ID_MUST_NOT_NULL),
            userId = userId,
            amount = amount,
            bidTime = currentTime,
            sequence = sequence
        )
    }

    fun update(
        title: String,
        startPrice: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ) {
        if (this.status != AuctionStatus.PENDING &&
            this.status != AuctionStatus.READY && 
            this.status != AuctionStatus.APPROVED) {
            throw BusinessException(ErrorCode.AUCTION_ALREADY_STARTED)
        }
        this.title = title
        this.startPrice = startPrice
        this.currentPrice = startPrice
        this.startTime = startTime
        this.endTime = endTime
    }

    /**
     * 경매 시작
     * READY/APPROVED -> ONGOING
     */
    fun startAuction() {
        if (this.status != AuctionStatus.READY && this.status != AuctionStatus.APPROVED) {
            throw BusinessException(ErrorCode.ACTION_NOT_READY)
        }

        this.status = AuctionStatus.ONGOING
    }

    /**
     * 경매 종료
     * ONGOING -> ENDED
     */
    fun closeAuction() {
        if (this.status != AuctionStatus.ONGOING) {
            throw BusinessException(ErrorCode.AUCTION_NOT_ONGOING)
        }
        this.status = AuctionStatus.ENDED
    }

    /**
     * 낙찰
     * ENDED -> COMPLETED
     */
    fun completeAuction() {
        if (this.status != AuctionStatus.ENDED) {
            throw BusinessException(ErrorCode.AUCTION_NOT_ENDED)
        }
        this.status = AuctionStatus.COMPLETED
    }

    /**
     * 유찰
     * ENDED -> FAILED
     */
    fun failAuction() {
        if (this.status != AuctionStatus.ENDED) {
            throw BusinessException(ErrorCode.AUCTION_NOT_ENDED)
        }
        this.status = AuctionStatus.FAILED
    }

    /**
     * 승인 (PENDING -> APPROVED)
     */
    fun approve() {
        if (this.status != AuctionStatus.PENDING) {
            throw BusinessException(ErrorCode.AUCTION_NOT_PENDING)
        }
        this.status = AuctionStatus.APPROVED
    }

    /**
     * 거절 (PENDING -> REJECTED)
     */
    fun reject(reason: String) {
        if (this.status != AuctionStatus.PENDING) {
            throw BusinessException(ErrorCode.AUCTION_NOT_PENDING)
        }
        this.status = AuctionStatus.REJECTED
    }

    /**
     * 삭제
     */
    fun delete() {
        this.deleted = true
    }

    private fun verifyStatus() {
        if (this.status != AuctionStatus.ONGOING) {
            throw BusinessException(ErrorCode.AUCTION_NOT_ONGOING)
        }
    }

    private fun verifyTime(
        currentTime: LocalDateTime
    ) {
        if (currentTime.isBefore(startTime)) {
            throw BusinessException(ErrorCode.AUCTION_NOT_READY)
        }
        if (currentTime.isAfter(endTime)) {
            throw BusinessException(ErrorCode.AUCTION_ALREADY_ENDED)
        }
    }

    private fun verifyPrice(
        amount: Long
    ) {
        if (amount <= currentPrice) {
            throw BusinessException(ErrorCode.BID_AMOUNT_HIGHER_THAN_CURRENT_PRICE)
        }
    }

    fun endAuction() = closeAuction()
}
