package com.ch.auction.domain

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.interfaces.common.ErrorCode
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "auctions")
class Auction(
    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val startPrice: Long,

    @Column(nullable = false)
    val startTime: LocalDateTime,

    @Column(nullable = false)
    val endTime: LocalDateTime,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
    @Column(nullable = false)
    var currentPrice: Long = startPrice
        private set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AuctionStatus = AuctionStatus.READY
        private set

    /**
     * 입찰
     */
    fun placeBid(
        userId: Long,
        amount: Long,
        currentTime: LocalDateTime = LocalDateTime.now()
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
            bidTime = currentTime
        )
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
    fun endAuction() {
        if (this.status != AuctionStatus.ONGOING) {
            throw BusinessException(ErrorCode.AUCTION_NOT_ONGOING)
        }

        this.status = AuctionStatus.ENDED
    }
}

