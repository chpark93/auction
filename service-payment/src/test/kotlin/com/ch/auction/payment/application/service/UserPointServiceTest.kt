package com.ch.auction.payment.application.service

import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.payment.domain.UserPointInfo
import com.ch.auction.payment.domain.UserPointRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("UserPointService 단위 테스트")
class UserPointServiceTest {

    private val userPointRepository: UserPointRepository = mockk()
    private lateinit var userPointService: UserPointService

    @BeforeEach
    fun setUp() {
        userPointService = UserPointService(
            userPointRepository = userPointRepository
        )
    }

    @Test
    @DisplayName("포인트 충전 시 잔액이 정상 증가")
    fun charge_point_increases_balance() {
        // given
        val userId = 1L
        val chargeAmount = 10000L
        val previousBalance = 5000L
        val expectedBalance = 15000L

        val request = PointDTOs.PointRequest(
            amount = chargeAmount
        )

        every {
            userPointRepository.chargePoint(
                userId = userId,
                amount = chargeAmount
            )
        } returns expectedBalance

        every {
            userPointRepository.getPoint(
                userId = userId
            )
        } returns UserPointInfo(
            totalPoint = expectedBalance,
            lockedPoint = 0L
        )

        // when
        val result = userPointService.chargePoint(
            userId = userId,
            request = request
        )

        // then
        assertEquals(expectedBalance, result.totalPoint)
        assertEquals(0L, result.lockedPoint)
        assertEquals(expectedBalance, result.availablePoint)

        verify(exactly = 1) {
            userPointRepository.chargePoint(
                userId = userId,
                amount = chargeAmount
            )
        }

        verify(exactly = 1) {
            userPointRepository.getPoint(
                userId = userId
            )
        }
    }

    @Test
    @DisplayName("포인트 조회 시 totalPoint, lockedPoint, availablePoint가 정확하게 반환")
    fun get_point_returns_accurate_info() {
        // given
        val userId = 1L
        val totalPoint = 50000L
        val lockedPoint = 10000L
        val expectedAvailablePoint = 40000L

        every {
            userPointRepository.getPoint(
                userId = userId
            )
        } returns UserPointInfo(
            totalPoint = totalPoint,
            lockedPoint = lockedPoint
        )

        // when
        val result = userPointService.getPoint(
            userId = userId
        )

        // then
        assertEquals(userId, result.userId)
        assertEquals(totalPoint, result.totalPoint)
        assertEquals(lockedPoint, result.lockedPoint)
        assertEquals(expectedAvailablePoint, result.availablePoint)

        verify(exactly = 1) {
            userPointRepository.getPoint(
                userId = userId
            )
        }
    }

    @Test
    @DisplayName("포인트 충전 후 availablePoint가 정확하게 계산")
    fun charge_point_available_point_calculation() {
        // given
        val userId = 1L
        val chargeAmount = 20000L
        val totalPoint = 30000L
        val lockedPoint = 5000L
        val expectedAvailablePoint = 25000L

        val request = PointDTOs.PointRequest(
            amount = chargeAmount
        )

        every {
            userPointRepository.chargePoint(
                userId = userId,
                amount = chargeAmount
            )
        } returns totalPoint

        every {
            userPointRepository.getPoint(
                userId = userId
            )
        } returns UserPointInfo(
            totalPoint = totalPoint,
            lockedPoint = lockedPoint
        )

        // when
        val result = userPointService.chargePoint(
            userId = userId,
            request = request
        )

        // then
        assertEquals(expectedAvailablePoint, result.availablePoint)
        assertEquals(totalPoint - lockedPoint, result.availablePoint)
    }

    @Test
    @DisplayName("여러 번 포인트 충전 시 누적 금액이 정확하게 반영")
    fun multiple_charge_accumulates_correctly() {
        // given
        val userId = 1L
        val charges = listOf(10000L, 20000L, 15000L)
        val expectedFinalBalance = 45000L

        var currentBalance = 0L
        charges.forEach { chargeAmount ->
            currentBalance += chargeAmount

            val request = PointDTOs.PointRequest(
                amount = chargeAmount
            )

            every {
                userPointRepository.chargePoint(
                    userId = userId,
                    amount = chargeAmount
                )
            } returns currentBalance

            every {
                userPointRepository.getPoint(
                    userId = userId
                )
            } returns UserPointInfo(
                totalPoint = currentBalance,
                lockedPoint = 0L
            )

            // when
            val result = userPointService.chargePoint(
                userId = userId,
                request = request
            )

            // then
            assertEquals(currentBalance, result.totalPoint)
        }

        assertEquals(expectedFinalBalance, currentBalance)

        verify(exactly = charges.size) {
            userPointRepository.chargePoint(
                userId = userId,
                amount = any()
            )
        }
    }
}

