package com.ch.auction.infrastructure.redis

import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class RedisMessageSubscriber(
    private val messagingTemplate: SimpMessagingTemplate
) : MessageListener {

    override fun onMessage(
        message: Message,
        pattern: ByteArray?
    ) {
        // Redis Topic에서 수신한 메시지 본문
        val body = String(message.body)
        
        // 메시지 형식: "auctionId:price" 또는 JSON
        // 여기서는 단순하게 파싱하거나, 메시지 자체를 DTO JSON이라고 가정하고 그대로 클라이언트에 전송
        // 실제로는 Channel(Topic) 이름에서 auctionId를 추출하거나 메시지 내용에 포함해야 함.
        
        // 구현 가정: Redis Topic 이름이 "auction-price" 하나이고, 메시지 내용에 { "auctionId": 1, "price": 1000 } 형태의 JSON이 들어있다고 가정.
        // 만약 Topic을 경매별로 나눈다면 ("auction:1", "auction:2") pattern 매칭을 통해 auctionId를 알 수 있음.
        // 요구사항: "입찰 성공 시 변경된 가격 정보를 Redis Topic으로 발행"
        
        // 여기서는 모든 경매의 업데이트를 하나의 Topic("auction-updates")으로 받고, 
        // 내용에 auctionId가 포함되어 있다고 가정하여 클라이언트의 특정 토픽("/topic/auctions/{id}")으로 전송합니다.
        
        // 간단한 JSON 파싱 (혹은 ObjectMapper 사용) - 여기서는 문자열 처리 예시
        // 실제 프로덕션에서는 ObjectMapper 사용 권장
        
        // 메시지를 그대로 /topic/auction-updates 로 쏘거나, 
        // 파싱해서 /topic/auction/{id} 로 쏘는 로직이 필요함.
        // 여기서는 ObjectMapper를 주입받지 않고 단순 String 전달로 구현하되,
        // Subscriber가 수신한 메시지를 "/topic/auction-updates"로 브로드캐스팅한다고 가정.
        
        messagingTemplate.convertAndSend("/topic/auction-updates", body)
    }
}

