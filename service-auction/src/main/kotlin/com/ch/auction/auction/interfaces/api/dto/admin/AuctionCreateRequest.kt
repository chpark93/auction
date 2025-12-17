package com.ch.auction.auction.interfaces.api.dto.admin

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

data class AuctionCreateRequest(
    @field:NotNull(message = "상품 ID는 필수입니다.")
    val productId: Long,

    @field:NotNull(message = "시작 가격은 필수입니다.")
    @field:Positive(message = "시작 가격은 양수여야 합니다.")
    val startPrice: Long,

    @field:NotNull(message = "시작 시간은 필수입니다.")
    val startTime: LocalDateTime,

    @field:NotNull(message = "종료 시간은 필수입니다.")
    val endTime: LocalDateTime
)

