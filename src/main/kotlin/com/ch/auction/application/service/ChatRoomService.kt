package com.ch.auction.application.service

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.domain.AuctionStatus
import com.ch.auction.domain.chat.ChatRoom
import com.ch.auction.infrastructure.persistence.AuctionJpaRepository
import com.ch.auction.infrastructure.persistence.OrderRepository
import com.ch.auction.infrastructure.persistence.chat.ChatRoomRepository
import com.ch.auction.interfaces.common.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatRoomService(
    private val chatRoomRepository: ChatRoomRepository,
    private val auctionRepository: AuctionJpaRepository,
    private val orderRepository: OrderRepository
) {

    @Transactional(readOnly = true)
    fun getOrCreateChatRoom(
        auctionId: Long,
        requestUserId: Long
    ): ChatRoom {
        val auction = auctionRepository.findById(auctionId)
            .orElseThrow { BusinessException(ErrorCode.AUCTION_NOT_FOUND) }

        if (auction.status != AuctionStatus.COMPLETED) {
            throw BusinessException(ErrorCode.AUCTION_NOT_ENDED)
        }

        val order = orderRepository.findByAuctionId(
            auctionId = auctionId
        ).orElseThrow { BusinessException(ErrorCode.ORDER_NOT_FOUND) }

        if (auction.sellerId != requestUserId && order.buyerId != requestUserId) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }
        
        val sellerId = auction.sellerId
        val buyerId = order.buyerId

        return chatRoomRepository.findByAuctionId(
            auctionId = auctionId
        ).orElseGet {
            chatRoomRepository.save(
                ChatRoom(
                    auctionId = auctionId,
                    sellerId = sellerId,
                    buyerId = buyerId
                )
            )
        }
    }

    fun verifyChatRoomAccess(
        roomId: String,
        userId: Long
    ) {
        val chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow { BusinessException(ErrorCode.RESOURCE_NOT_FOUND) }
        
        if (chatRoom.sellerId != userId && chatRoom.buyerId != userId) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }
    }
}
