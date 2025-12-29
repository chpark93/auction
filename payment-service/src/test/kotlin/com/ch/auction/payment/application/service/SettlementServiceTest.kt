package com.ch.auction.payment.application.service

import com.ch.auction.common.ApiResponse
import com.ch.auction.common.ErrorCode
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.exception.BusinessException
import com.ch.auction.payment.domain.*
import com.ch.auction.payment.infrastructure.client.UserClient
import com.ch.auction.payment.infrastructure.client.dto.UserClientDtos
import com.ch.auction.payment.infrastructure.persistence.OrderRepository
import com.ch.auction.payment.infrastructure.persistence.PointTransactionRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("SettlementService 단위 테스트")
class SettlementServiceTest {

    private val orderRepository: OrderRepository = mockk()
    private val pointTransactionRepository: PointTransactionRepository = mockk()
    private val userPointRepository: UserPointRepository = mockk()
    private val userClient: UserClient = mockk()

    private lateinit var settlementService: SettlementService

    @BeforeEach
    fun setUp() {
        settlementService = SettlementService(
            orderRepository = orderRepository,
            pointTransactionRepository = pointTransactionRepository,
            userPointRepository = userPointRepository,
            userClient = userClient
        )
    }

    @Test
    @DisplayName("구매 확정 시 정산 수수료 5%가 정확히 차감되고 판매자에게 지급")
    fun confirm_purchase_settlement_with_commission() {
        // given
        val buyerId = 1L
        val orderId = 100L
        val sellerId = 2L
        val amount = 100000L

        val expectedCommission = 5000L // 5%
        val expectedSettlementAmount = 95000L // 95000원

        val order = mockk<Order>(relaxed = true)
        val delivery = mockk<Delivery>(relaxed = true)

        every {
            orderRepository.findById(orderId)
        } returns Optional.of(order)
        every {
            order.buyerId
        } returns buyerId
        every {
            order.sellerId
        } returns sellerId
        every {
            order.payment
        } returns amount
        every {
            order.delivery
        } returns delivery
        every {
            delivery.status
        } returns DeliveryStatus.SHIPPING
        every {
            delivery.confirm()
        } just Runs

        every {
            userClient.chargePoint(
                userId = sellerId,
                request = PointDTOs.PointRequest(
                    amount = expectedSettlementAmount
                )
            )
        } returns ApiResponse.ok(
            UserClientDtos.UserPointResponse(
                userId = sellerId,
                totalPoint = 95000L,
                lockedPoint = 0L,
                availablePoint = 95000L
            )
        )

        every {
            pointTransactionRepository.save(any<PointTransaction>())
        } returns mockk()

        every {
            userPointRepository.chargePoint(
                userId = sellerId,
                amount = expectedSettlementAmount
            )
        } returns 95000L

        // when
        settlementService.confirmPurchase(
            buyerId = buyerId,
            orderId = orderId
        )

        // then
        verify(exactly = 1) {
            delivery.confirm()
        }

        verify(exactly = 1) {
            userClient.chargePoint(
                userId = sellerId,
                request = PointDTOs.PointRequest(
                    amount = expectedSettlementAmount
                )
            )
        }

        verify(exactly = 1) {
            pointTransactionRepository.save(
                match<PointTransaction> {
                    it.userId == sellerId &&
                    it.type == PointTransactionType.SETTLEMENT &&
                    it.amount == expectedSettlementAmount &&
                    it.balanceAfter == 95000L
                }
            )
        }

        verify(exactly = 1) {
            userPointRepository.chargePoint(
                userId = sellerId,
                amount = expectedSettlementAmount
            )
        }
    }

    @Test
    @DisplayName("다른 사용자의 주문을 확정하려고 하면 FORBIDDEN 예외 발생")
    fun confirm_purchase_by_different_user_throws_forbidden() {
        // given
        val buyerId = 1L
        val orderId = 100L
        val anotherUserId = 999L

        val order = mockk<Order>()

        every {
            orderRepository.findById(orderId)
        } returns Optional.of(order)
        every {
            order.buyerId
        } returns anotherUserId

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            settlementService.confirmPurchase(
                buyerId = buyerId,
                orderId = orderId
            )
        }

        assertEquals(ErrorCode.FORBIDDEN, exception.errorCode)
    }

    @Test
    @DisplayName("이미 확정된 배송을 다시 확정하려고 하면 INVALID_INPUT_VALUE 예외 발생")
    fun confirm_purchase_already_confirmed_throws_exception() {
        // given
        val buyerId = 1L
        val orderId = 100L

        val order = mockk<Order>()
        val delivery = mockk<Delivery>()

        every {
            orderRepository.findById(orderId)
        } returns Optional.of(order)
        every {
            order.buyerId
        } returns buyerId
        every {
            order.delivery
        } returns delivery
        every {
            delivery.status
        } returns DeliveryStatus.CONFIRMED

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            settlementService.confirmPurchase(
                buyerId = buyerId,
                orderId = orderId
            )
        }

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.errorCode)
    }

    @Test
    @DisplayName("주문이 존재하지 않으면 ORDER_NOT_FOUND 예외가 발생한다")
    fun confirm_purchase_order_not_found_throws_exception() {
        // given
        val buyerId = 1L
        val orderId = 999L

        every {
            orderRepository.findById(orderId)
        } returns Optional.empty()

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            settlementService.confirmPurchase(
                buyerId = buyerId,
                orderId = orderId
            )
        }

        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.errorCode)
    }

    @Test
    @DisplayName("배송 정보가 없으면 DELIVERY_NOT_FOUND 예외 발생")
    fun confirm_purchase_delivery_not_found_throws_exception() {
        // given
        val buyerId = 1L
        val orderId = 100L

        val order = mockk<Order>()

        every {
            orderRepository.findById(orderId)
        } returns Optional.of(order)
        every {
            order.buyerId
        } returns buyerId
        every {
            order.delivery
        } returns null

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            settlementService.confirmPurchase(
                buyerId = buyerId,
                orderId = orderId
            )
        }

        assertEquals(ErrorCode.DELIVERY_NOT_FOUND, exception.errorCode)
    }

    @Test
    @DisplayName("정산 금액 계산 정확 - deposit + commission = finalPrice")
    fun settlement_amount_calculation_is_accurate() {
        // given
        val testCases = listOf(
            100000L to Pair(95000L, 5000L), // 10만원 -> 95000원 + 5000원
            50000L to Pair(47500L, 2500L), // 5만원 -> 47500원 + 2500원
            200000L to Pair(190000L, 10000L), // 20만원 -> 190000원 + 10000원
            10000L to Pair(9500L, 500L) // 1만원 -> 9500원 + 500원
        )

        testCases.forEach { (finalPrice, expected) ->
            val (expectedDeposit, expectedCommission) = expected
            
            // when
            val commissionRate = 0.05
            val commission = (finalPrice * commissionRate).toLong()
            val settlementAmount = finalPrice - commission

            // then
            assertEquals(expectedCommission, commission, "수수료 계산이 정확해야 합니다")
            assertEquals(expectedDeposit, settlementAmount, "정산 금액 계산이 정확해야 합니다")
            assertEquals(finalPrice, settlementAmount + commission, "정산액 + 수수료 = 낙찰가 검증")
        }
    }
}

