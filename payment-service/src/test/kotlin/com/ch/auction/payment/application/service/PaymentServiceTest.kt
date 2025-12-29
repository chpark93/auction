package com.ch.auction.payment.application.service

import com.ch.auction.common.ApiResponse
import com.ch.auction.common.ErrorCode
import com.ch.auction.common.dto.PointDTOs
import com.ch.auction.exception.BusinessException
import com.ch.auction.payment.domain.*
import com.ch.auction.payment.infrastructure.client.UserClient
import com.ch.auction.payment.infrastructure.client.dto.UserClientDtos
import com.ch.auction.payment.infrastructure.persistence.PaymentRepository
import com.ch.auction.payment.infrastructure.persistence.PaymentTransactionRepository
import com.ch.auction.payment.infrastructure.persistence.PointTransactionRepository
import com.ch.auction.payment.interfaces.api.dto.payment.PaymentRequest
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("PaymentService 단위 테스트")
class PaymentServiceTest {

    private val paymentRepository: PaymentRepository = mockk()
    private val paymentTransactionRepository: PaymentTransactionRepository = mockk()
    private val pointTransactionRepository: PointTransactionRepository = mockk()
    private val userPointRepository: UserPointRepository = mockk()
    private val paymentProvider: PaymentProvider = mockk()
    private val userClient: UserClient = mockk()

    private lateinit var paymentService: PaymentService

    @BeforeEach
    fun setUp() {
        paymentService = PaymentService(
            paymentRepository = paymentRepository,
            paymentTransactionRepository = paymentTransactionRepository,
            pointTransactionRepository = pointTransactionRepository,
            userPointRepository = userPointRepository,
            paymentProvider = paymentProvider,
            userClient = userClient
        )
    }

    @Test
    @DisplayName("경매 낙찰 시 구매자 포인트가 정상적으로 차감")
    fun settle_auction_deducts_buyer_point() {
        // given
        val userId = 1L
        val auctionAmount = 50000L
        val remainingPoint = 50000L // 차감 후 잔액

        every {
            userClient.usePoint(
                userId = userId,
                request = PointDTOs.PointRequest(
                    amount = auctionAmount
                )
            )
        } returns ApiResponse.ok(
            UserClientDtos.UserPointResponse(
                userId = userId,
                totalPoint = remainingPoint,
                lockedPoint = 0L,
                availablePoint = remainingPoint
            )
        )

        every {
            pointTransactionRepository.save(any<PointTransaction>())
        } returns mockk()

        every {
            userPointRepository.usePoint(
                userId = userId,
                amount = auctionAmount
            )
        } just Runs

        // when
        paymentService.settleAuction(
            userId = userId,
            amount = auctionAmount
        )

        // then
        verify(exactly = 1) {
            userClient.usePoint(
                userId = userId,
                request = PointDTOs.PointRequest(
                    amount = auctionAmount
                )
            )
        }

        verify(exactly = 1) {
            pointTransactionRepository.save(
                match<PointTransaction> {
                    it.userId == userId &&
                    it.type == PointTransactionType.USE &&
                    it.amount == auctionAmount &&
                    it.balanceAfter == remainingPoint
                }
            )
        }

        verify(exactly = 1) {
            userPointRepository.usePoint(
                userId = userId,
                amount = auctionAmount
            )
        }
    }

    @Test
    @DisplayName("포인트 충전 시 결제 검증이 실패하면 PAYMENT_VERIFICATION_FAILED 예외 발생")
    fun charge_point_payment_verification_failed() {
        // given
        val email = "user@test.com"
        val request = PaymentRequest(
            orderId = "ORDER-123",
            transactionKey = "TRANSACTION-KEY",
            amount = 10000L
        )

        every {
            userClient.getUserByEmail(
                email = email
            )
        } returns ApiResponse.ok(
            UserClientDtos.UserPointResponse(
                userId = 1L,
                totalPoint = 0L,
                lockedPoint = 0L,
                availablePoint = 0L
            )
        )

        every {
            paymentTransactionRepository.existsByTransactionKey(
                transactionKey = request.transactionKey
            )
        } returns false

        every {
            paymentProvider.validatePayment(
                transactionKey = request.transactionKey,
                amount = request.amount
            )
        } returns false

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            paymentService.chargePoint(
                email = email,
                request = request
            )
        }

        assertEquals(ErrorCode.PAYMENT_VERIFICATION_FAILED, exception.errorCode)
    }

    @Test
    @DisplayName("중복된 transactionKey로 결제 시 DUPLICATE_PAYMENT 예외 발생")
    fun charge_point_duplicate_transaction_key() {
        // given
        val email = "user@test.com"
        val request = PaymentRequest(
            orderId = "ORDER-123",
            transactionKey = "DUPLICATE-KEY",
            amount = 10000L
        )

        every {
            userClient.getUserByEmail(
                email = email
            )
        } returns ApiResponse.ok(
            UserClientDtos.UserPointResponse(
                userId = 1L,
                totalPoint = 0L,
                lockedPoint = 0L,
                availablePoint = 0L
            )
        )

        every {
            paymentTransactionRepository.existsByTransactionKey(
                transactionKey = request.transactionKey
            )
        } returns true

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            paymentService.chargePoint(
                email = email,
                request = request
            )
        }

        assertEquals(ErrorCode.DUPLICATE_PAYMENT, exception.errorCode)
    }

    @Test
    @DisplayName("포인트 충전 시 Payment와 PaymentTransaction이 정상적으로 저장")
    fun charge_point_saves_payment_and_transaction() {
        // given
        val email = "user@test.com"
        val userId = 1L
        val chargeAmount = 20000L

        val request = PaymentRequest(
            orderId = "ORDER-123",
            transactionKey = "TRANSACTION-KEY",
            amount = chargeAmount
        )

        every {
            userClient.getUserByEmail(
                email = email
            )
        } returns ApiResponse.ok(
            UserClientDtos.UserPointResponse(
                userId = userId,
                totalPoint = 0L,
                lockedPoint = 0L,
                availablePoint = 0L
            )
        )

        every {
            paymentTransactionRepository.existsByTransactionKey(
                transactionKey = request.transactionKey
            )
        } returns false

        every {
            paymentProvider.validatePayment(
                transactionKey = request.transactionKey,
                amount = request.amount
            )
        } returns true

        every {
            paymentRepository.save(any<Payment>())
        } returns mockk()

        every {
            userClient.chargePoint(
                userId = userId,
                request = PointDTOs.PointRequest(
                    amount = chargeAmount
                )
            )
        } returns ApiResponse.ok(
            UserClientDtos.UserPointResponse(
                userId = userId,
                totalPoint = chargeAmount,
                lockedPoint = 0L,
                availablePoint = chargeAmount
            )
        )

        every {
            pointTransactionRepository.save(any<PointTransaction>())
        } returns mockk()

        every {
            userPointRepository.chargePoint(
                userId = userId,
                amount = chargeAmount
            )
        } returns chargeAmount

        // when
        paymentService.chargePoint(
            email = email,
            request = request
        )

        // then
        verify(exactly = 1) {
            paymentRepository.save(any<Payment>())
        }

        verify(exactly = 1) {
            pointTransactionRepository.save(
                match<PointTransaction> {
                    it.userId == userId &&
                    it.type == PointTransactionType.CHARGE &&
                    it.amount == chargeAmount
                }
            )
        }

        verify(exactly = 1) {
            userPointRepository.chargePoint(
                userId = userId,
                amount = chargeAmount
            )
        }
    }

    @Test
    @DisplayName("경매 낙찰 시 사용자가 존재하지 않으면 USER_NOT_FOUND 예외 발생")
    fun settle_auction_user_not_found() {
        // given
        val userId = 999L
        val amount = 50000L

        every {
            userClient.usePoint(
                userId = userId,
                request = PointDTOs.PointRequest(
                    amount = amount
                )
            )
        } returns ApiResponse(
            success = true,
            data = null
        )

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            paymentService.settleAuction(
                userId = userId,
                amount = amount
            )
        }

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
    }
}

