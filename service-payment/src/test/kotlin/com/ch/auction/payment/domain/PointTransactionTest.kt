package com.ch.auction.payment.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("PointTransaction 도메인 단위 테스트")
class PointTransactionTest {

    @Test
    @DisplayName("포인트 충전 트랜잭션 생성 시 모든 필드가 정확하게 설정")
    fun create_charge_transaction() {
        // given
        val userId = 1L
        val type = PointTransactionType.CHARGE
        val amount = 10000L
        val balanceAfter = 15000L

        // when
        val transaction = PointTransaction.create(
            userId = userId,
            type = type,
            amount = amount,
            balanceAfter = balanceAfter
        )

        // then
        assertEquals(userId, transaction.userId)
        assertEquals(type, transaction.type)
        assertEquals(amount, transaction.amount)
        assertEquals(balanceAfter, transaction.balanceAfter)
    }

    @Test
    @DisplayName("포인트 사용 트랜잭션 생성 시 모든 필드가 정확하게 설정")
    fun create_use_transaction() {
        // given
        val userId = 1L
        val type = PointTransactionType.USE
        val amount = 5000L
        val balanceAfter = 10000L

        // when
        val transaction = PointTransaction.create(
            userId = userId,
            type = type,
            amount = amount,
            balanceAfter = balanceAfter
        )

        // then
        assertEquals(userId, transaction.userId)
        assertEquals(type, transaction.type)
        assertEquals(amount, transaction.amount)
        assertEquals(balanceAfter, transaction.balanceAfter)
    }

    @Test
    @DisplayName("정산 트랜잭션 생성 시 모든 필드가 정확하게 설정")
    fun create_settlement_transaction() {
        // given
        val userId = 2L
        val type = PointTransactionType.SETTLEMENT
        val amount = 95000L // 5% 수수료 차감
        val balanceAfter = 95000L

        // when
        val transaction = PointTransaction.create(
            userId = userId,
            type = type,
            amount = amount,
            balanceAfter = balanceAfter
        )

        // then
        assertEquals(userId, transaction.userId)
        assertEquals(type, transaction.type)
        assertEquals(amount, transaction.amount)
        assertEquals(balanceAfter, transaction.balanceAfter)
    }

    @Test
    @DisplayName("환불 트랜잭션 생성 시 모든 필드가 정확하게 설정")
    fun create_refund_transaction() {
        // given
        val userId = 1L
        val type = PointTransactionType.REFUND
        val amount = 5000L
        val balanceAfter = 20000L

        // when
        val transaction = PointTransaction.create(
            userId = userId,
            type = type,
            amount = amount,
            balanceAfter = balanceAfter
        )

        // then
        assertEquals(userId, transaction.userId)
        assertEquals(type, transaction.type)
        assertEquals(amount, transaction.amount)
        assertEquals(balanceAfter, transaction.balanceAfter)
    }
}

