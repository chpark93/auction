package com.ch.auction.interfaces.api

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.application.service.ChatRoomService
import com.ch.auction.application.service.ChatService
import com.ch.auction.domain.chat.ChatMessage
import com.ch.auction.infrastructure.persistence.UserRepository
import com.ch.auction.interfaces.api.dto.chat.ChatDTOs
import com.ch.auction.interfaces.common.ApiResponse
import com.ch.auction.interfaces.common.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
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
    private val userRepository: UserRepository
) {

    // TODO: ChatMessageController로 분리
    @MessageMapping("/chat/send")
    fun sendMessage(
        request: ChatDTOs.ChatMessageRequest,
        principal: Principal
    ) {
        val email = principal.name
        val user = userRepository.findByEmail(
            email = email
        ).orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        val savedMessage = chatService.saveMessage(
            ChatMessage(
                roomId = request.roomId,
                senderId = user.id!!,
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
    ): ApiResponse<ChatDTOs.ChatRoomResponse> {
        val user = userRepository.findByEmail(
            email = email
        ).orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        val chatRoom = chatRoomService.getOrCreateChatRoom(
            auctionId = auctionId,
            requestUserId = user.id!!
        )

        return ApiResponse.ok(
            ChatDTOs.ChatRoomResponse(
                roomId = chatRoom.id!!,
                auctionId = chatRoom.auctionId,
                sellerId = chatRoom.sellerId,
                buyerId = chatRoom.buyerId
            )
        )
    }

    @ResponseBody
    @GetMapping("/api/v1/chat/rooms/{roomId}/messages")
    fun getMessages(
        @PathVariable roomId: String,
        @PageableDefault(size = 20, sort = ["timestamp"]) pageable: Pageable,
        @AuthenticationPrincipal email: String
    ): ApiResponse<Page<ChatMessage>> {
        
        val user = userRepository.findByEmail(
            email = email
        ).orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        
        chatRoomService.verifyChatRoomAccess(
            roomId = roomId,
            userId = user.id!!
        )
        
        val messages = chatService.getMessages(
            roomId = roomId,
            pageable = pageable
        )

        return ApiResponse.ok(messages)
    }
}
