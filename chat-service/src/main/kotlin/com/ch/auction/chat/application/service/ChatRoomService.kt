package com.ch.auction.chat.application.service

import com.ch.auction.chat.domain.ChatRoom
import com.ch.auction.chat.infrastructure.client.AuctionClient
import com.ch.auction.chat.infrastructure.client.OrderClient
import com.ch.auction.chat.infrastructure.client.UserClient
import com.ch.auction.chat.infrastructure.persistence.ChatRoomRepository
import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatRoomService(
    private val chatRoomRepository: ChatRoomRepository,
    private val auctionClient: AuctionClient,
    private val orderClient: OrderClient,
    private val userClient: UserClient
) {

    @Transactional(readOnly = true)
    fun getOrCreateChatRoom(
        auctionId: Long,
        requestUserId: Long
    ): ChatRoom {
        val auctionResponse = auctionClient.getAuction(
            auctionId = auctionId
        ).data ?: throw BusinessException(ErrorCode.AUCTION_NOT_FOUND)

        if (auctionResponse.status != "COMPLETED") {
            throw BusinessException(ErrorCode.AUCTION_NOT_ENDED)
        }

        val orderResponse = orderClient.getOrderByAuctionId(
            auctionId = auctionId
        ).data ?: throw BusinessException(ErrorCode.ORDER_NOT_FOUND)

        if (auctionResponse.sellerId != requestUserId &&
            orderResponse.buyerId != requestUserId) {

            throw BusinessException(ErrorCode.FORBIDDEN)
        }
        
        val sellerId = auctionResponse.sellerId
        val buyerId = orderResponse.buyerId

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
