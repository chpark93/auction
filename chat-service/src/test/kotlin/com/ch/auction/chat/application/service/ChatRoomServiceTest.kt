package com.ch.auction.chat.application.service

import com.ch.auction.chat.domain.ChatRoom
import com.ch.auction.chat.infrastructure.client.AuctionClient
import com.ch.auction.chat.infrastructure.client.OrderClient
import com.ch.auction.chat.infrastructure.client.UserClient
import com.ch.auction.chat.infrastructure.client.dto.AuctionDtos
import com.ch.auction.chat.infrastructure.client.dto.OrderClientDtos
import com.ch.auction.chat.infrastructure.persistence.ChatRoomRepository
import com.ch.auction.common.ApiResponse
import com.ch.auction.common.ErrorCode
import com.ch.auction.exception.BusinessException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class ChatRoomServiceTest {

    private val chatRoomRepository: ChatRoomRepository = mockk()
    private val auctionClient: AuctionClient = mockk()
    private val orderClient: OrderClient = mockk()
    private val userClient: UserClient = mockk()

    private lateinit var chatRoomService: ChatRoomService

    @BeforeEach
    fun setUp() {
        chatRoomService = ChatRoomService(
            chatRoomRepository = chatRoomRepository,
            auctionClient = auctionClient,
            orderClient = orderClient,
            userClient = userClient
        )
    }

    @Test
    @DisplayName("채팅방 생성 성공 - 낙찰자(buyerId)가 요청할 때")
    fun get_or_create_chat_room_success_buyer_request() {
        // given
        val auctionId = 1L
        val sellerId = 10L
        val buyerId = 20L
        val requestUserId = buyerId // 낙찰자가 요청

        val auctionResponse = AuctionDtos.AuctionResponse(
            id = auctionId,
            sellerId = sellerId,
            status = "COMPLETED"
        )

        val orderResponse = OrderClientDtos.OrderResponse(
            id = 100L,
            buyerId = buyerId,
            auctionId = auctionId
        )

        val chatRoom = ChatRoom(
            id = "room-1",
            auctionId = auctionId,
            sellerId = sellerId,
            buyerId = buyerId
        )

        every {
            auctionClient.getAuction(
                auctionId = auctionId
            )
        } returns ApiResponse.ok(auctionResponse)
        every {
            orderClient.getOrderByAuctionId(
                auctionId = auctionId
            )
        } returns ApiResponse.ok(orderResponse)
        every {
            chatRoomRepository.findByAuctionId(
                auctionId = auctionId
            )
        } returns Optional.of(chatRoom)

        // when
        val result = chatRoomService.getOrCreateChatRoom(
            auctionId = auctionId,
            requestUserId = requestUserId
        )

        // then
        assertNotNull(result)
        assertEquals(auctionId, result.auctionId)
        assertEquals(sellerId, result.sellerId)
        assertEquals(buyerId, result.buyerId)

        verify(exactly = 1) {
            auctionClient.getAuction(
                auctionId = auctionId
            )
        }
        verify(exactly = 1) {
            orderClient.getOrderByAuctionId(
                auctionId = auctionId
            )
        }
        verify(exactly = 1) {
            chatRoomRepository.findByAuctionId(
                auctionId = auctionId
            )
        }
    }

    @Test
    @DisplayName("채팅방 생성 성공 - 판매자(sellerId)가 요청할 때")
    fun get_or_create_chat_room_success_seller_request() {
        // given
        val auctionId = 1L
        val sellerId = 10L
        val buyerId = 20L
        val requestUserId = sellerId // 판매자가 요청

        val auctionResponse = AuctionDtos.AuctionResponse(
            id = auctionId,
            sellerId = sellerId,
            status = "COMPLETED"
        )

        val orderResponse = OrderClientDtos.OrderResponse(
            id = 100L,
            buyerId = buyerId,
            auctionId = auctionId
        )

        val chatRoom = ChatRoom(
            id = "room-1",
            auctionId = auctionId,
            sellerId = sellerId,
            buyerId = buyerId
        )

        every {
            auctionClient.getAuction(
                auctionId = auctionId
            )
        } returns ApiResponse.ok(auctionResponse)
        every {
            orderClient.getOrderByAuctionId(
                auctionId = auctionId
            )
        } returns ApiResponse.ok(orderResponse)
        every {
            chatRoomRepository.findByAuctionId(
                auctionId = auctionId
            )
        } returns Optional.of(chatRoom)

        // when
        val result = chatRoomService.getOrCreateChatRoom(
            auctionId = auctionId,
            requestUserId = requestUserId
        )

        // then
        assertNotNull(result)
        assertEquals(auctionId, result.auctionId)
        assertEquals(sellerId, result.sellerId)
        assertEquals(buyerId, result.buyerId)

        verify(exactly = 1) {
            auctionClient.getAuction(
                auctionId = auctionId
            )
        }
        verify(exactly = 1) {
            orderClient.getOrderByAuctionId(
                auctionId = auctionId
            )
        }
        verify(exactly = 1) {
            chatRoomRepository.findByAuctionId(
                auctionId = auctionId
            )
        }
    }

    @Test
    @DisplayName("채팅방 생성 성공 - 채팅방이 없을 때 새로 생성")
    fun get_or_create_chat_room_success_create_new_room() {
        // given
        val auctionId = 1L
        val sellerId = 10L
        val buyerId = 20L
        val requestUserId = buyerId

        val auctionResponse = AuctionDtos.AuctionResponse(
            id = auctionId,
            sellerId = sellerId,
            status = "COMPLETED"
        )

        val orderResponse = OrderClientDtos.OrderResponse(
            id = 100L,
            buyerId = buyerId,
            auctionId = auctionId
        )

        val newChatRoom = ChatRoom(
            id = "new-room-1",
            auctionId = auctionId,
            sellerId = sellerId,
            buyerId = buyerId
        )

        every {
            auctionClient.getAuction(
                auctionId = auctionId
            )
        } returns ApiResponse.ok(auctionResponse)
        every {
            orderClient.getOrderByAuctionId(
                auctionId = auctionId
            )
        } returns ApiResponse.ok(orderResponse)
        every {
            chatRoomRepository.findByAuctionId(
                auctionId = auctionId
            )
        } returns Optional.empty()
        every {
            chatRoomRepository.save(any<ChatRoom>())
        } returns newChatRoom

        // when
        val result = chatRoomService.getOrCreateChatRoom(auctionId, requestUserId)

        // then
        assertNotNull(result)
        assertEquals(auctionId, result.auctionId)
        assertEquals(sellerId, result.sellerId)
        assertEquals(buyerId, result.buyerId)

        verify(exactly = 1) {
            auctionClient.getAuction(
                auctionId = auctionId
            )
        }
        verify(exactly = 1) {
            orderClient.getOrderByAuctionId(
                auctionId = auctionId
            )
        }
        verify(exactly = 1) {
            chatRoomRepository.findByAuctionId(
                auctionId = auctionId
            )
        }
        verify(exactly = 1) {
            chatRoomRepository.save(any<ChatRoom>())
        }
    }

    @Test
    @DisplayName("채팅방 생성 실패 - 제3자가 요청할 때 FORBIDDEN 예외 발생")
    fun get_or_create_chat_room_fail_third_party_request() {
        // given
        val auctionId = 1L
        val sellerId = 10L
        val buyerId = 20L
        val thirdPartyUserId = 30L // 제3자

        val auctionResponse = AuctionDtos.AuctionResponse(
            id = auctionId,
            sellerId = sellerId,
            status = "COMPLETED"
        )

        val orderResponse = OrderClientDtos.OrderResponse(
            id = 100L,
            buyerId = buyerId,
            auctionId = auctionId
        )

        every {
            auctionClient.getAuction(
                auctionId = auctionId
            )
        } returns ApiResponse.ok(auctionResponse)
        every {
            orderClient.getOrderByAuctionId(
                auctionId = auctionId
            )
        } returns ApiResponse.ok(orderResponse)

        // when & then
        val exception = assertThrows<BusinessException> {
            chatRoomService.getOrCreateChatRoom(
                auctionId = auctionId,
                requestUserId = thirdPartyUserId
            )
        }

        assertEquals(ErrorCode.FORBIDDEN, exception.errorCode)

        verify(exactly = 1) {
            auctionClient.getAuction(
                auctionId = auctionId
            )
        }
        verify(exactly = 1) {
            orderClient.getOrderByAuctionId(
                auctionId = auctionId
            )
        }
        verify(exactly = 0) {
            chatRoomRepository.findByAuctionId(
                auctionId = any()
            )
        }
        verify(exactly = 0) {
            chatRoomRepository.save(any<ChatRoom>())
        }
    }

    @Test
    @DisplayName("채팅방 생성 실패 - 경매가 ONGOING 상태일 때 AUCTION_NOT_ENDED 예외 발생")
    fun get_or_create_chat_room_fail_auction_ongoing() {
        // given
        val auctionId = 1L
        val sellerId = 10L
        val requestUserId = sellerId

        val auctionResponse = AuctionDtos.AuctionResponse(
            id = auctionId,
            sellerId = sellerId,
            status = "ONGOING"
        )

        every {
            auctionClient.getAuction(
                auctionId = auctionId
            )
        } returns ApiResponse.ok(auctionResponse)

        // when & then
        val exception = assertThrows<BusinessException> {
            chatRoomService.getOrCreateChatRoom(
                auctionId = auctionId,
                requestUserId = requestUserId
            )
        }

        assertEquals(ErrorCode.AUCTION_NOT_ENDED, exception.errorCode)

        verify(exactly = 1) {
            auctionClient.getAuction(
                auctionId = auctionId
            )
        }
        verify(exactly = 0) {
            orderClient.getOrderByAuctionId(
                auctionId = any()
            )
        }
        verify(exactly = 0) {
            chatRoomRepository.findByAuctionId(
                auctionId = any()
            )
        }
    }

    @Test
    @DisplayName("채팅방 생성 실패 - 경매를 찾을 수 없을 때 AUCTION_NOT_FOUND 예외 발생")
    fun get_or_create_chat_room_fail_auction_not_found() {
        // given
        val auctionId = 1L
        val requestUserId = 10L

        every {
            auctionClient.getAuction(
                auctionId = auctionId
            )
        } returns ApiResponse(
            success = true,
            data = null
        )

        // when & then
        val exception = assertThrows<BusinessException> {
            chatRoomService.getOrCreateChatRoom(
                auctionId = auctionId,
                requestUserId = requestUserId
            )
        }

        assertEquals(ErrorCode.AUCTION_NOT_FOUND, exception.errorCode)

        verify(exactly = 1) {
            auctionClient.getAuction(
                auctionId = auctionId
            )
        }
        verify(exactly = 0) {
            orderClient.getOrderByAuctionId(
                auctionId = any()
            )
        }
        verify(exactly = 0) {
            chatRoomRepository.findByAuctionId(
                auctionId = any()
            )
        }
    }

    @Test
    @DisplayName("채팅방 생성 실패 - 주문을 찾을 수 없을 때 ORDER_NOT_FOUND 예외 발생")
    fun get_or_create_chat_room_fail_order_not_found() {
        // given
        val auctionId = 1L
        val sellerId = 10L
        val requestUserId = sellerId

        val auctionResponse = AuctionDtos.AuctionResponse(
            id = auctionId,
            sellerId = sellerId,
            status = "COMPLETED"
        )

        every {
            auctionClient.getAuction(
                auctionId = auctionId
            )
        } returns ApiResponse.ok(auctionResponse)
        every {
            orderClient.getOrderByAuctionId(
                auctionId = auctionId
            )
        } returns ApiResponse(
            success = true,
            data = null
        )

        // when & then
        val exception = assertThrows<BusinessException> {
            chatRoomService.getOrCreateChatRoom(
                auctionId = auctionId,
                requestUserId = requestUserId
            )
        }

        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.errorCode)

        verify(exactly = 1) {
            auctionClient.getAuction(
                auctionId = auctionId
            )
        }
        verify(exactly = 1) {
            orderClient.getOrderByAuctionId(
                auctionId = auctionId
            )
        }
        verify(exactly = 0) {
            chatRoomRepository.findByAuctionId(
                auctionId = any()
            )
        }
    }

    @Test
    @DisplayName("채팅방 접근 권한 검증 성공 - 판매자가 접근")
    fun verify_chat_room_access_success_seller() {
        // given
        val roomId = "room-1"
        val sellerId = 10L
        val buyerId = 20L

        val chatRoom = ChatRoom(
            id = roomId,
            auctionId = 1L,
            sellerId = sellerId,
            buyerId = buyerId
        )

        every {
            chatRoomRepository.findById(roomId)
        } returns Optional.of(chatRoom)

        // when & then
        chatRoomService.verifyChatRoomAccess(
            roomId = roomId,
            userId = sellerId
        )

        verify(exactly = 1) {
            chatRoomRepository.findById(roomId)
        }
    }

    @Test
    @DisplayName("채팅방 접근 권한 검증 성공 - 구매자가 접근")
    fun verify_chat_room_access_success_buyer() {
        // given
        val roomId = "room-1"
        val sellerId = 10L
        val buyerId = 20L

        val chatRoom = ChatRoom(
            id = roomId,
            auctionId = 1L,
            sellerId = sellerId,
            buyerId = buyerId
        )

        every {
            chatRoomRepository.findById(roomId)
        } returns Optional.of(chatRoom)

        // when & then
        chatRoomService.verifyChatRoomAccess(
            roomId = roomId,
            userId = buyerId
        )

        verify(exactly = 1) {
            chatRoomRepository.findById(roomId)
        }
    }

    @Test
    @DisplayName("채팅방 접근 권한 검증 실패 - 제3자가 접근 시 FORBIDDEN 예외 발생")
    fun verify_chat_room_access_fail_third_party() {
        // given
        val roomId = "room-1"
        val sellerId = 10L
        val buyerId = 20L
        val thirdPartyUserId = 30L

        val chatRoom = ChatRoom(
            id = roomId,
            auctionId = 1L,
            sellerId = sellerId,
            buyerId = buyerId
        )

        every {
            chatRoomRepository.findById(roomId)
        } returns Optional.of(chatRoom)

        // when & then
        val exception = assertThrows<BusinessException> {
            chatRoomService.verifyChatRoomAccess(
                roomId = roomId,
                userId = thirdPartyUserId
            )
        }

        assertEquals(ErrorCode.FORBIDDEN, exception.errorCode)

        verify(exactly = 1) {
            chatRoomRepository.findById(roomId)
        }
    }

    @Test
    @DisplayName("채팅방 접근 권한 검증 실패 - 채팅방을 찾을 수 없을 때 RESOURCE_NOT_FOUND 예외 발생")
    fun verify_chat_room_access_fail_room_not_found() {
        // given
        val roomId = "non-existent-room"
        val userId = 10L

        every {
            chatRoomRepository.findById(roomId)
        } returns Optional.empty()

        // when & then
        val exception = assertThrows<BusinessException> {
            chatRoomService.verifyChatRoomAccess(
                roomId = roomId,
                userId = userId
            )
        }

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.errorCode)

        verify(exactly = 1) {
            chatRoomRepository.findById(roomId)
        }
    }
}

