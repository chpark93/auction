package com.ch.auction.auction.infrastructure.sse

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@Component
class SseEmitterManager {

    private val logger = LoggerFactory.getLogger(javaClass)

    // auctionId -> Set<SseEmitter>
    private val emitters = ConcurrentHashMap<Long, MutableSet<SseEmitter>>()

    /**
     * 새로운 SSE 연결 추가
     */
    fun addEmitter(
        auctionId: Long,
        emitter: SseEmitter
    ) {
        emitters.computeIfAbsent(auctionId) {
            ConcurrentHashMap.newKeySet()
        }.add(emitter)

        logger.info("SSE emitter added for auction $auctionId. Total: ${emitters[auctionId]?.size}")

        emitter.onCompletion {
            removeEmitter(
                auctionId = auctionId,
                emitter = emitter
            )
        }
        emitter.onTimeout {
            removeEmitter(
                auctionId = auctionId,
                emitter = emitter
            )
        }
        emitter.onError {
            removeEmitter(
                auctionId = auctionId,
                emitter = emitter
            )
        }
    }

    /**
     * SSE 연결 제거
     */
    private fun removeEmitter(
        auctionId: Long,
        emitter: SseEmitter
    ) {
        emitters[auctionId]?.remove(emitter)
        if (emitters[auctionId]?.isEmpty() == true) {
            emitters.remove(auctionId)
        }

        logger.info("SSE emitter removed for auction $auctionId. Remaining: ${emitters[auctionId]?.size ?: 0}")
    }

    /**
     * 특정 경매의 모든 구독자 -> 메시지 전송
     */
    fun sendToAuction(
        auctionId: Long,
        eventName: String,
        data: Any
    ) {
        val auctionEmitters = emitters[auctionId] ?: return
        val deadEmitters = mutableSetOf<SseEmitter>()

        auctionEmitters.forEach { emitter ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name(eventName)
                        .data(data)
                )
                logger.debug("SSE message sent to auction $auctionId: $eventName")
            } catch (e: Exception) {
                logger.warn("Failed to send SSE message to auction $auctionId", e)
                deadEmitters.add(emitter)
            }
        }

        // 실패한 연결들 제거
        deadEmitters.forEach { emitter ->
            removeEmitter(auctionId, emitter)
        }
    }

    /**
     * 현재 연결 수 조회
     */
    fun getConnectionCount(
        auctionId: Long
    ): Int {
        return emitters[auctionId]?.size ?: 0
    }
}

