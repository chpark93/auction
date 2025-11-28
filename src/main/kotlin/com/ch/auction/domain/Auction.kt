package com.ch.auction.domain

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.interfaces.common.ErrorCode
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@Table(name = "auctions")
@SQLRestriction("deleted = false")
class Auction(
    title: String,
    startPrice: Long,
    startTime: LocalDateTime,
    endTime: LocalDateTime,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
    @Column(nullable = false)
    var title: String = title
        private set

    @Column(nullable = false)
    var startPrice: Long = startPrice
        private set

    @Column(nullable = false)
    var startTime: LocalDateTime = startTime
        private set

    @Column(nullable = false)
    var endTime: LocalDateTime = endTime
        private set

    @Column(nullable = false)
    var currentPrice: Long = startPrice
        private set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AuctionStatus = AuctionStatus.READY
        private set

    @Column(nullable = false)
    var deleted: Boolean = false
        private set

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

        return Bid(
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
        if (this.status != AuctionStatus.READY) {
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
     * READY -> ONGOING
     */
    fun startAuction() {
        if (this.status != AuctionStatus.READY) {
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
     * 낙찰 완료
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
