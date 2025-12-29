package com.ch.auction.chat.interfaces.api

import com.ch.auction.chat.application.service.ChatRoomService
import com.ch.auction.chat.application.service.ChatService
import com.ch.auction.chat.domain.ChatMessage
import com.ch.auction.chat.infrastructure.client.UserClient
import com.ch.auction.chat.interfaces.api.dto.ChatDTOs
import com.ch.auction.common.ApiResponse
import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.LocalDateTime

@Controller
class ChatController(
    private val chatService: ChatService,
    private val chatRoomService: ChatRoomService,
    private val messagingTemplate: SimpMessagingTemplate,
    private val userClient: UserClient
) {

    // TODO: ChatMessageController로 분리
    @MessageMapping("/chat/send")
    fun sendMessage(
        request: ChatDTOs.ChatMessageRequest,
        principal: Principal
    ) {
        val email = principal.name
        val userResponse = userClient.getUserByEmail(
            email = email
        ).data ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
        
        val savedMessage = chatService.saveMessage(
            ChatMessage(
                roomId = request.roomId,
                senderId = userResponse.userId,
                message = request.message,
                timestamp = LocalDateTime.now()
            )
        )

        messagingTemplate.convertAndSend("/topic/chat/${request.roomId}", savedMessage)
    }
    
    @ResponseBody
    @PostMapping("/api/v1/chat/rooms")
    fun enterChatRoom(
        @RequestParam auctionId: Long,
        @AuthenticationPrincipal email: String
    ): ResponseEntity<ApiResponse<ChatDTOs.ChatRoomResponse>> {
        val userResponse = userClient.getUserByEmail(
            email = email
        ).data ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
        
        val chatRoom = chatRoomService.getOrCreateChatRoom(
            auctionId = auctionId,
            requestUserId = userResponse.userId
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = ChatDTOs.ChatRoomResponse(
                    roomId = chatRoom.id!!,
                    auctionId = chatRoom.auctionId,
                    sellerId = chatRoom.sellerId,
                    buyerId = chatRoom.buyerId
                )
            )
        )
    }

    @ResponseBody
    @GetMapping("/api/v1/chat/rooms/{roomId}/messages")
    fun getMessages(
        @PathVariable roomId: String,
        @PageableDefault(size = 20, sort = ["timestamp"]) pageable: Pageable,
        @AuthenticationPrincipal email: String
    ): ResponseEntity<ApiResponse<Page<ChatMessage>>> {
        
        val userResponse = userClient.getUserByEmail(
            email = email
        ).data ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
        
        chatRoomService.verifyChatRoomAccess(
            roomId = roomId,
            userId = userResponse.userId
        )
        
        val messages = chatService.getMessages(
            roomId = roomId,
            pageable = pageable
        )

        return ResponseEntity.ok(
            ApiResponse.ok(
                data = messages
            )
        )
    }
}
