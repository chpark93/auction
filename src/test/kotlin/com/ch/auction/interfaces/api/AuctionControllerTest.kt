package com.ch.auction.interfaces.api

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.application.service.AuctionService
import com.ch.auction.interfaces.api.dto.BidRequest
import com.ch.auction.interfaces.common.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

@WebMvcTest(AuctionController::class)
class AuctionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var auctionService: AuctionService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun auction_success_200_ok() {
        // given
        val auctionId = 1L
        val amount = BigDecimal("5000")
        val request = BidRequest(
            userId = 100L,
            amount = amount
        )

        `when`(
            auctionService.placeBid(
                safeEq(auctionId),
                safeEq(request.userId),
                safeEq(request.amount)
            )
        ).thenReturn(amount)

        // when & then
        mockMvc.perform(
            post("/api/v1/auctions/$auctionId/bid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.newPrice").value(5000))
    }

    @Test
    fun auction_failed_40_bad_request() {
        // given
        val auctionId = 1L
        val request = BidRequest(
            userId = 100L,
            amount = BigDecimal("5000")
        )

        `when`(
            auctionService.placeBid(
                safeEq(auctionId),
                safeEq(request.userId),
                safeEq(request.amount)
            )
        ).thenThrow(BusinessException(ErrorCode.PRICE_TOO_LOW))

        // when & then
        mockMvc.perform(
            post("/api/v1/auctions/$auctionId/bid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value(ErrorCode.PRICE_TOO_LOW.code))
    }

    private fun <T> safeEq(
        value: T
    ): T = Mockito.eq(value) ?: value
}
