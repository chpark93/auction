package com.ch.auction.auction.interfaces.api

import com.ch.auction.auction.infrastructure.sse.SseEmitterManager
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Tag(name = "Auction SSE", description = "경매 실시간 업데이트 API")
@RestController
@RequestMapping("/api/v1/auctions")
class AuctionSseController(
    private val sseEmitterManager: SseEmitterManager
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(summary = "경매 실시간 업데이트 구독", description = "SSE를 통해 경매의 실시간 입찰 정보를 받습니다")
    @GetMapping("/{auctionId}/subscribe", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(
        @PathVariable auctionId: Long
    ): SseEmitter {
        // SSE 타임아웃: 30분
        val emitter = SseEmitter(30 * 60 * 1000L)

        try {
            // 연결 확인용 초기 메시지 전송
            emitter.send(
                SseEmitter.event()
                    .name("connected")
                    .data(mapOf("auctionId" to auctionId))
            )
            
            sseEmitterManager.addEmitter(auctionId, emitter)
            logger.info("New SSE connection for auction $auctionId")
        } catch (e: Exception) {
            logger.error("Failed to create SSE emitter for auction $auctionId", e)
            emitter.completeWithError(e)
        }

        return emitter
    }

    @Operation(summary = "경매 구독자 수 조회")
    @GetMapping("/{auctionId}/subscribers/count")
    fun getSubscriberCount(
        @PathVariable auctionId: Long
    ): Int {
        return sseEmitterManager.getConnectionCount(auctionId)
    }
}

