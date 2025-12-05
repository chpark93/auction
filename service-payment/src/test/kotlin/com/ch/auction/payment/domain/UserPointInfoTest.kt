package com.ch.auction.payment.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("UserPointInfo 도메인 단위 테스트")
class UserPointInfoTest {

    @Test
    @DisplayName("availablePoint는 totalPoint - lockedPoint로 정확하게 계산")
    fun available_point_calculation() {
        // given
        val totalPoint = 100000L
        val lockedPoint = 30000L
        val expectedAvailablePoint = 70000L

        // when
        val userPointInfo = UserPointInfo(
            totalPoint = totalPoint,
            lockedPoint = lockedPoint
        )

        // then
        assertEquals(expectedAvailablePoint, userPointInfo.availablePoint)
    }

    @Test
    @DisplayName("lockedPoint가 0일 때 availablePoint는 totalPoint와 같다")
    fun available_point_equals_total_when_no_locked() {
        // given
        val totalPoint = 50000L
        val lockedPoint = 0L

        // when
        val userPointInfo = UserPointInfo(
            totalPoint = totalPoint,
            lockedPoint = lockedPoint
        )

        // then
        assertEquals(totalPoint, userPointInfo.availablePoint)
    }

    @Test
    @DisplayName("모든 포인트가 잠겨있을 때 availablePoint는 0이다")
    fun available_point_is_zero_when_all_locked() {
        // given
        val totalPoint = 50000L
        val lockedPoint = 50000L

        // when
        val userPointInfo = UserPointInfo(
            totalPoint = totalPoint,
            lockedPoint = lockedPoint
        )

        // then
        assertEquals(0L, userPointInfo.availablePoint)
    }

    @Test
    @DisplayName("포인트가 없을 때 availablePoint는 0이다")
    fun available_point_is_zero_when_no_point() {
        // given
        val totalPoint = 0L
        val lockedPoint = 0L

        // when
        val userPointInfo = UserPointInfo(
            totalPoint = totalPoint,
            lockedPoint = lockedPoint
        )

        // then
        assertEquals(0L, userPointInfo.availablePoint)
    }

    @Test
    @DisplayName("다양한 포인트 상황에서 availablePoint가 정확하게 계산")
    fun available_point_calculation_various_cases() {
        // given
        val testCases = listOf(
            Triple(100000L, 20000L, 80000L), // 일반
            Triple(50000L, 0L, 50000L), // 잠금 없음
            Triple(30000L, 30000L, 0L), // 전체 잠금
            Triple(0L, 0L, 0L), // 포인트 없음
            Triple(1000000L, 500000L, 500000L) // 큰 금액
        )

        testCases.forEach { (total, locked, expected) ->
            // when
            val userPointInfo = UserPointInfo(
                totalPoint = total,
                lockedPoint = locked
            )

            // then
            assertEquals(expected, userPointInfo.availablePoint,
                "Total: $total, Locked: $locked should result in Available: $expected")
        }
    }
}

